'use strict';

angular.module('skyBestApp.order_form_controller', [])
  .controller('orderFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', function($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache) {
    $scope.status = 'Loading order...';

    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: "current"}, function(data) {
        $scope.currentUser = data;
      });
    }
    retrieveUser();
    $scope.$on("UserChanged", retrieveUser);

    $scope.orderSent = false;
      
    $scope.choices = {
      nodeTypes: { 
        APPLICATION_SERVER: 'Application Server', 
        WAS_DEPLOYMENT_MANAGER: 'WAS Deployment Manager',
        BPM_DEPLOYMENT_MANAGER: 'BPM Deployment Manager',
        BPM_NODES: 'BPM Nodes'
      },
      zones:  ['fss', 'sbs'],
      environmentClasses: ['u', 't', 'q', 'p'],
      environmentClassNames: {u: 'Utvikling', t: 'Test', q: 'PreProd', p: 'Produksjon'},
      serverCounts: [1, 2, 4, 8],
      serverSizes: {s: 'Liten', m: 'Medium', l: 'Stor'},
      applicationServerTypes: {jb: 'Jboss', wa: 'WAS'},
      applicationServerTypeMessages: {},
      defaults: { 
        APPLICATION_SERVER: {
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: '',
          applicationName: '', 
          serverCount: 1,
          serverSize: 's',
          disk: false,
          applicationServerType: null
        }, 
        WAS_DEPLOYMENT_MANAGER: { 
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: ''
        },
        BPM_DEPLOYMENT_MANAGER: {
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: ''
        },
        BPM_NODES: {
          environmentClass: 'u', 
          zone: 'fss',
          environmentName: ''
        }
      },
    };

    $scope.nodeType = 'APPLICATION_SERVER';

    $scope.settings = $scope.choices.defaults[$scope.nodeType];
    
    $scope.busies = {}; 

    $scope.errors = {
        form_errors: {}
    };
    
    function clearErrorHandler(name) {
      $rootScope.$broadcast('GeneralError', { removeName: name });
    }
    
    function errorHandler(name, busyIndicator) {
      return function(data, status, headers, config) {
        if (busyIndicator)
          delete $scope.busies[busyIndicator];
        $rootScope.$broadcast('GeneralError', { name: name, httpError: { data: data, status: status, headers: headers, config: config }});
      };
    };
    
    function isReady() {
      $scope.errors.form_errors = {
        general: []
      };
      var validations = 
        [{ value: $scope.settings.environmentName, target: "environmentName_error", message: "Miljønavn må spesifiseres" },       
         { value: $scope.currentUser && $scope.currentUser.authenticated, target: "general", message: "Du må være innlogget for å legge inn en bestilling" }, 
         { value: $scope.settings.applicationName, target: "applicationName_error", message: "Applikasjonsnavn må spesifiseres"},
         { value: $scope.settings.applicationServerType, target: "applicationServerType_error", message: "Mellomvaretype må spesifiseres" }];
      var hasError = _.reduce(validations, function(memo, validation) {
        if (validation.value === undefined) {
          return memo;
        }
        if (!validation.value) {
          var targetArray = $scope.errors.form_errors[validation.target];
          if (_.isArray(targetArray)) {
            targetArray.push(validation.message);
          } else {
            $scope.errors.form_errors[validation.target] = validation.message;
          }
        }
        return memo && validation.value;
      }, true);
      return hasError;
    };
    	    
    function xml2json(data, getter) {
      var contentType = getter()['content-type'];
      if (contentType && contentType.match('application/xml')) 
        return new X2JS().xml_str2json(data);
      return {}; 
    }
    
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
    
    function doAll() {
      var functions = arguments;
      return function() {
        var fargs = arguments;
        _(functions).each(function(f) { f.apply(this, fargs); });
      };
    }
    
    function checkExistingWasDeploymentManager() {
      var tasks = arguments;
      function condition(a) { return a.condition === undefined || a.condition(); }
      if (_(tasks).find(condition)) {
        $http({ method: 'GET', url: 'rest/domains', params: {envClass: $scope.settings.environmentClass, zone: $scope.settings.zone}})
          .success(function(domain) {
            var query = { 
                domain: domain,
                envClass: $scope.settings.environmentClass, 
                envName: $scope.settings.environmentName, 
                type: 'DeploymentManager', 
                app: $scope.settings.applicationName, 
                alias: 'wasDmgr'
            };
            $http({ method: 'GET', url: 'api/helper/fasit/resources/bestmatch', params: query, transformResponse: xml2json })
              .success(doAll.apply(this, _.chain(tasks).filter(condition).pluck('success').filter(_.isFunction).value()))
              .error(doAll.apply(this, _.chain(tasks).filter(condition).pluck('error').filter(_.isFunction).value()));
          }).error(errorHandler('Domener'));
      }
    }
    
    var checkWasDeploymentManagerDependency = {
      condition: function() { return $scope.nodeType == 'APPLICATION_SERVER'; },
      success: function(data) {
          clearErrorHandler('DeploymentManager');
          delete $scope.choices.applicationServerTypeMessages.wa; 
        },
      error: function(data, status, headers, config) {
          if (status == 404) { 
            clearErrorHandler('DeploymentManager');
            $scope.choices.applicationServerTypeMessages.wa = "DomainManager ikke funnet i gitt miljø";
            if ($scope.settings.applicationServerType == 'wa') {
              $scope.settings.applicationServerType = null;
            }
          } else errorHandler('DeploymentManager')(data, status, headers, config);
        } 
    };
    
    var checkRedundantDeploymentManager = {
        condition: function() { return $scope.nodeType == 'WAS_DEPLOYMENT_MANAGER'; },
        success: function(data) {
          $rootScope.$broadcast('GeneralError', { name: 'Deployment Manager', message: 'WAS deployment manager eksisterer allerede i gitt miljø og sone' });
        },
        error: function(data, status, headers, config) {
          if (status == 404) { 
            clearErrorHandler('Deployment Manager');
          } else errorHandler('Deployment Manager')(data, status, headers, config);
        }
    };

    $scope.$watch('nodeType', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      console.log('Not equal ' + newVal + ' and ' + oldVal);
      $scope.settings = $scope.choices.defaults[newVal];
    });

    $scope.$watch('settings.zone', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      checkExistingWasDeploymentManager(checkWasDeploymentManagerDependency, checkRedundantDeploymentManager);
    });

    $scope.$watch('settings.environmentName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      delete $scope.errors.form_errors.environmentName_error;
      checkExistingWasDeploymentManager(checkWasDeploymentManagerDependency, checkRedundantDeploymentManager);
    });

    $scope.$watch('settings.applicationName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      delete $scope.errors.form_errors.applicationName_error;
      checkExistingWasDeploymentManager(checkWasDeploymentManagerDependency);
    });

    $scope.$watch('settings.applicationServerType', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      delete $scope.errors.form_errors.applicationServerType_error;
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
        $scope.orderSent = true;
        $scope.busies.orderSend = true;
        $http.post("rest/orders", $scope.settings).success(function(order) {
          delete $scope.busies.orderSend;
          $location.path('/order_list').search({ id: order.id });
        }).error(errorHandler('Ordreinnsending', 'orderSend'));
      }
    };

  }]);
