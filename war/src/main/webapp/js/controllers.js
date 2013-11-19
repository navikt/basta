'use strict';

/* Controllers */

angular.module('skyBestApp.controllers', [])

  .controller('mainController', ['$scope', '$http', '$templateCache', '$location', '$resource', function($scope, $http, $templateCache, $location, $resource) {
    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: "current"}, function(data) {
        $scope.currentUser = data;
      });
    }

    $scope.clearCache = function() {
        $templateCache.removeAll();
        console.log('Template cache cleared...');
    };
    $scope.createOrder = function() {
      $location.path('/order');
    };
    $scope.showLogin = function() {
      $scope.userForm = {};
    };
    $scope.login = function() {
      var config = { headers: { 'Content-Type': 'application/x-www-form-urlencoded' }};
      var data = $.param({ j_username: $scope.userForm.username, j_password: $scope.userForm.password });
      $http.post('security-check', data, config).success(function(data, status, headers, config) {
        if (data == 'success') {
          $scope.userForm = null;
          $scope.$broadcast("UserChanged");
        } else {
          $scope.userForm.error = 'Innlogging feilet';
        }
      });
    };
    $scope.logout = function() {
      $http.get('logout');
      $scope.$broadcast("UserChanged");
    };

    $scope.createTemplate = function() {
      $location.path('/template');
    };
    
    retrieveUser();
    $scope.$on("UserChanged", retrieveUser);
}])


  .controller('orderFormController', ['$scope', '$http', '$routeParams', '$resource', '$location', '$templateCache', function($scope, $http, $routeParams, $resource, $location, $templateCache) {
    $scope.status = 'Loading order...';

    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: "current"}, function(data) {
        $scope.currentUser = data;
      });
    }
    retrieveUser();
    $scope.$on("UserChanged", retrieveUser);

    $scope.order = {};

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
      console.log($scope.errors);
      return hasError;
    };
    	    
    $scope.choices = {
      zones:  ['fss', 'sbs'],
      environmentClasses: ['utv', 'test', 'qa', 'prod'],
      serverCounts: [1, 2, 4, 8],
      serverSizes: {s: 'Liten', m: 'Medium', l: 'Stor'},
      applicationServerTypes: {jb: 'Jboss', wa: 'WAS'}
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
    
    $scope.$watch('settings.environmentName', function(newVal, oldVal) {
      if(newVal == oldVal) { return; }
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
        $http.post("rest/orders?dryRun=true", $scope.settings).success(function() {
          alert("Yeah!");
        });
      }
    };

  }]);
