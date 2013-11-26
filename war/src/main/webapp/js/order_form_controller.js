'use strict';

angular.module('skyBestApp.order_form_controller', [])
  .controller('orderFormController', ['$scope', '$http', '$routeParams', '$resource', '$location', '$templateCache', function($scope, $http, $routeParams, $resource, $location, $templateCache) {
    $scope.status = 'Loading order...';

    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: "current"}, function(data) {
        $scope.currentUser = data;
      });
    }
    retrieveUser();
    $scope.$on("UserChanged", retrieveUser);

    $scope.settings = {
      environmentClass: 'utv', 
      multisite: false, 
      zone: 'fss',
      environmentName: '',
      applicationName: '', 
      serverCount: 1,
      serverSize: 's',
      disk: false,
      applicationServerType: null
    };
    
    $scope.choices = {
      zones:  ['fss', 'sbs'],
      environmentClasses: ['utv', 'test', 'qa', 'prod'],
      serverCounts: [1, 2, 4, 8],
      serverSizes: {s: 'Liten', m: 'Medium', l: 'Stor'},
      applicationServerTypes: {jb: 'Jboss', wa: 'WAS'},
      applicationServerTypeMessages: {}
    };
      
    $scope.errors= {};
    
    function isReady() {
      $scope.errors = {
        general_errors: []
      };
      var validations = 
        [{ value: $scope.settings.environmentName, target: "environmentName_error", message: "Miljønavn må spesifiseres" },       
         { value: $scope.currentUser && $scope.currentUser.authenticated, target: "general_errors", message: "Du må være innlogget for å legge inn en bestilling" }, 
         { value: $scope.settings.applicationName, target: "applicationName_error", message: "Applikasjonsnavn må spesifiseres"},
         { value: $scope.settings.applicationServerType, target: "applicationServerType_error", message: "Mellomvaretype må spesifiseres" }];
      var hasError = _.reduce(validations, function(memo, validation) {
        if (!validation.value) {
          var targetArray = $scope.errors[validation.target];
          if (_.isArray(targetArray)) {
            targetArray.push(validation.message);
          } else {
            $scope.errors[validation.target] = validation.message;
          }
        }
        return memo && validation.value;
      }, true);
      return hasError;
    };
    	    
    function xml2json(data) {
      return new X2JS().xml_str2json(data);
    }
    
    $scope.hasZone = function(zone) {
      return !(zone == 'sbs' && $scope.settings.environmentClass == 'utv');
    };
    $scope.hasEnvironmentClassAccess = function(environmentClass) {
      if ($scope.currentUser) { 
        var classes = $scope.currentUser.environmentClasses;
        return classes.indexOf(environmentClass.substring(0, 1)) > -1;
      }
      return false;
    };
    
    $http({ method: 'GET', url: '/api/helper/fasit/environments', transformResponse: xml2json }).success(function(data) {
      $scope.choices.environments = _.chain(data.collection.environment).groupBy('envClass').map(function(e, k) {
        return [k, _.map(e, function(e) { return e.name; })];
      }).object().value();
    });
    $http({ method: 'GET', url: '/api/helper/fasit/applications', transformResponse: xml2json }).success(function(data) {
      $scope.choices.applications = _.map(data.collection.application, function(a) {return a.name;});
    });
    
    function updateDomainManager() {
      // TODO what about domain/zone
      var query = { 
          envClass: $scope.settings.environmentClass.substring(0, 1), 
          envName: $scope.settings.environmentName, 
          type: 'DeploymentManager', 
          app: $scope.settings.applicationName 
      };
      $http({ method: 'GET', url: '/api/helper/fasit/resources/bestmatch', params: query, transformResponse: xml2json })
        .success(function(data) { delete $scope.choices.applicationServerTypeMessages.wa; })
        .error(function(data, status) { if (status == 404) { 
          $scope.choices.applicationServerTypeMessages.wa = "DomainManager ikke funnet i gitt miljø";
          if ($scope.settings.applicationServerType == 'wa') {
            $scope.settings.applicationServerType = null;
          }
        }});
    }
    
    $scope.$watch('settings.environmentName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      delete $scope.errors.environmentName_error;
      updateDomainManager();
    });

    $scope.$watch('settings.applicationName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      delete $scope.errors.applicationName_error;
      updateDomainManager();
    });

    $scope.$watch('settings.applicationServerType', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
      delete $scope.errors.applicationServerType_error;
    });

    $scope.$watch('settings.environmentClass', function(newVal, oldVal) {
        if(newVal == oldVal) { return; }
        if($scope.settings.environmentClass == 'utv') {
          $scope.settings.zone = 'fss';
          $scope.settings.multisite = false;
        } else if ($scope.settings.environmentClass == 'test') {
          $scope.settings.multisite = false;
        } else if ($scope.settings.environmentClass == 'prod') {
          $scope.settings.multisite = true;
        }
    });

    $scope.prepSave = function(statusText) {
        $scope.status = statusText;
    };

    $scope.submitOrder = function() {
      if (isReady()) {
        $http.post("rest/orders", $scope.settings).success(function(order) {
          $location.path('/order_list').search({ id: order.id });
        });
      }
    };

  }]);
