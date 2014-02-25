'use strict';

angular.module('skyBestApp.order_form_controller', [])
  .controller('orderFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', function($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache) {

    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: 'current'}, function(data) {
          $scope.currentUser = data;

      });
    }
    retrieveUser();
    $scope.$on('UserChanged', retrieveUser);

    $scope.choices = {
      zones:  ['fss', 'sbs'],
      environmentClasses: ['u', 't', 'q', 'p'],
      environmentClassNames: {u: 'Utvikling', t: 'Test', q: 'PreProd', p: 'Produksjon'},
      serverCounts: [1, 2, 4, 8],
      serverSizes: {s: {name: 'Standard'}, m: {name: 'Medium'}, l: {name: 'Stor'}},
      middleWareTypes: {jb: 'Jboss', wa: 'WAS'},
      middleWareTypeMessages: {},
      defaults: { 
        APPLICATION_SERVER: {
          nodeTypeName: 'Application Server',
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: '',
          applicationName: '', 
          serverCount: 1,
          serverSize: 's',
          disk: false,
          middleWareType: null
        }, 
        WAS_DEPLOYMENT_MANAGER: {
          nodeTypeName: 'WAS Deployment Manager',
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: ''
        },
        BPM_DEPLOYMENT_MANAGER: {
          nodeTypeName: 'BPM Deployment Manager',
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: '',
          commonDatasource: null,
          cellDatasource: null
        },
        BPM_NODES: {
          nodeTypeName: 'BPM Nodes', 
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: '',
          commonDatasource: null
        },
        PLAIN_LINUX: {
          nodeTypeName: 'Plain Linux Server',
          environmentClass: 'u', 
          zone: 'fss',
          serverSize: 's'
        }
      }
    };

    $scope.nodeType = 'APPLICATION_SERVER';
    $scope.busies = {}; 
    $scope.formErrors = { general: {} };
    $scope.orderSent = false;
    
    function clearErrorHandler(name) {
      $rootScope.$broadcast('General Error', { removeName: name });
    }
    
    function errorHandler(name, busyIndicator) {
      return function(data, status, headers, config) {
        if (busyIndicator)
          delete $scope.busies[busyIndicator];
        $rootScope.$broadcast('GeneralError', { name: name, httpError: { data: data, status: status, headers: headers, config: config }});
      };
    };


    function isReady() {
      var validations = 
        [{ value: $scope.settings.environmentName, target: ['environmentName_error'], message: 'Miljønavn må spesifiseres' },       
         { value: $scope.currentUser && $scope.currentUser.authenticated, target: ['general', 'authenticated'], message: 'Du må være innlogget for å legge inn en bestilling' }, 
         { value: $scope.settings.applicationName, target: ['applicationName_error'], message: 'Applikasjonsnavn må spesifiseres'},
         { value: $scope.settings.middleWareType, target: ['middleWareType_error'], message: 'Mellomvaretype må spesifiseres' },
         { value: $scope.settings.commonDatasource, target: ['commonDatasource_error'], message: 'Datakilde for common må spesifiseres' },
         { value: $scope.settings.cellDatasource, target: ['cellDatasource_error'], message: 'Datakilde for cell må spesifiseres' }];
      _.each(validations, function(validation) {
        if (!_.isUndefined(validation.value)) {
          withObjectInPath($scope.formErrors, validation.target, function(object, field) { delete object[field]; });
          if (!validation.value) {
            withObjectInPath($scope.formErrors, validation.target, function(object, field) { object[field] = validation.message; });
          }
        }
      });
      console.log($scope.formErrors);
      return !hasValidationErrors();
    };
    
    function hasValidationErrors() {
      return !_.chain($scope.formErrors).omit('general').isEmpty().value() || !_.isEmpty($scope.formErrors.general);
    }

  
    $scope.isEmpty = function(object) {
      return _.isEmpty(object); 
    };
    $scope.hasZone = function(zone) {
      return !(zone == 'sbs' && $scope.settings.environmentClass == 'u');
    };
    $scope.hasEnvironmentClassAccess = function(environmentClass) {
      if ($scope.currentUser) { 
        var classes = $scope.currentUser.environmentClasses;
        return classes.indexOf(environmentClass) > -1;
      }
      return false;
    };


    $scope.busies.environmentName = true;
    
    $http({ method: 'GET', url: 'api/helper/fasit/environments', transformResponse: xml2json }).success(function(data) {
    
      $scope.choices.environments = _.chain(data.collection.environment).groupBy('envClass').map(function(e, k) {
        delete $scope.busies.environmentName;
        return [k, _.chain(e).map(function(e) { return e.name; }).sortBy(_.identity).value()];
      }).object().value();
    }).error(errorHandler('Miljøliste', 'environmentName'));
    
    $scope.busies.applicationName = true;
    $http({ method: 'GET', url: 'api/helper/fasit/applications', transformResponse: xml2json }).success(function(data) {
      delete $scope.busies.applicationName;
      $scope.choices.applications = _.chain(data.collection.application).map(function(a) {return a.name;}).sortBy(_.identity).value();
    }).error(errorHandler('Applikasjonsliste', 'applicationName'));

    $http({ method: 'GET', url: 'rest/choices' }).success(function(data) {
      _($scope.choices.serverSizes).each(function(serverSize, name) { _(serverSize).extend(data.serverSizes[name]); });
    }).error(errorHandler('Valginformasjon'));

    function withDomain(f) {
      return $http({ method: 'GET', url: 'rest/domains', params: {envClass: $scope.settings.environmentClass, zone: $scope.settings.zone}})
        .success(f)
        .error(errorHandler('Domener'));
    }
    
    function checkExistingResource() {
      var tasks = arguments;
      function condition(a) {
          return a.condition === undefined || a.condition();
      }
      _.chain(tasks).filter(condition).each(function(task) {
        withDomain(function(domain) {
            $http({ method: 'GET', url: 'api/helper/fasit/resources/bestmatch', params: task.query(domain), transformResponse: xml2json })
              .success(task.success)
              .error(task.error);
          });
      });
    }

 
    function baseQuery(domain) {
      return { 
        domain: domain,
        envClass: $scope.settings.environmentClass, 
        envName: $scope.settings.environmentName, 
        app: $scope.settings.applicationName
      };
    }

    var checkWasDeploymentManagerDependency = {

      condition: function() {return $scope.nodeType == 'APPLICATION_SERVER'; },
      query: function(domain) { return _(baseQuery(domain)).extend({ alias: 'wasDmgr', type: 'DeploymentManager' }); },
      success: function(data) {
          clearErrorHandler('Deployment Manager');
          delete $scope.choices.middleWareTypeMessages.wa;
        },
      error: function(data, status, headers, config) {
          if (status == 404) {
            clearErrorHandler('Deployment Manager');
            $scope.choices.middleWareTypeMessages.wa = 'Deployment manager ikke funnet i gitt miljø';
            if ($scope.settings.middleWareType == 'wa') {
              $scope.settings.middleWareType = null;
            }
          } else errorHandler('Deployment Manager')(data, status, headers, config);
        }
    };
    
    var checkBpmDeploymentManagerDependency = {
      condition: function() { return $scope.nodeType == 'BPM_NODES'; },
      query: function(domain) { return _(baseQuery(domain)).extend({ alias: 'bpmDmgr', type: 'DeploymentManager' }); },
      success: function(data) {
          delete $scope.formErrors.general.bpmDeploymentManager;
        },
      error: function(data, status, headers, config) {
        if (status == 404) {
          clearErrorHandler('Deployment Manager');
          $scope.formErrors.general.bpmDeploymentManager = 'Deployment manager ikke funnet i gitt miljø';
        } else errorHandler('Deployment Manager')(data, status, headers, config);
      }
    };
    
    var checkRedundantDeploymentManager = {
      condition: function() { return $scope.nodeType == 'WAS_DEPLOYMENT_MANAGER'; },
      query: function(domain) { return _(baseQuery(domain)).extend({ alias: 'wasDmgr', type: 'DeploymentManager' }); },
      success: function(data) {
          $rootScope.$broadcast('General Error', { name: 'Deployment Manager', message: 'WAS deployment manager eksisterer allerede i gitt miljø og sone' });
        },
      error: function(data, status, headers, config) {
        if (status == 404) { 
          clearErrorHandler('Deployment Manager');
        } else errorHandler('Deployment Manager')(data, status, headers, config);
      }
    };

    $scope.$watch('nodeType', function(newVal, oldVal) {
      $scope.settings = _.omit($scope.choices.defaults[newVal], 'nodeTypeName');
      $scope.settings.nodeType = newVal;
      $scope.formErrors = { general: {} };
    });

    $scope.$watch('settings.zone', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      checkExistingResource(checkWasDeploymentManagerDependency, checkRedundantDeploymentManager, checkBpmDeploymentManagerDependency);
    });

    $scope.$watchCollection('settings', function() {
      if (hasValidationErrors()) {
        isReady();
      }
    });
    
    $scope.$watch('settings.environmentName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      checkExistingResource(checkWasDeploymentManagerDependency, checkRedundantDeploymentManager, checkBpmDeploymentManagerDependency);
    });

    $scope.$watch('settings.applicationName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      checkExistingResource(checkWasDeploymentManagerDependency);
    });

    $scope.$watch('settings.environmentClass', function(newVal, oldVal) {
        if(newVal == oldVal) { return; }
        if($scope.settings.environmentClass == 'u') {
          $scope.settings.zone = 'fss';
        }
    });

    $scope.prepSave = function(statusText) {
        $scope.status = statusText;
    };

    $scope.submitOrder = function() {
      if (isReady()) {
        $scope.settings.nodeType = $scope.nodeType;
        $scope.orderSent = true;
        $scope.busies.orderSend = true;
        $http.post('rest/orders', $scope.settings).success(function(order) {
          delete $scope.busies.orderSend;
          $location.path('/order_list').search({ id: order.id });
        }).error(errorHandler('Ordreinnsending', 'orderSend'));
      }
    };

  }]);
