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

    $scope.order = {
    		vm_data_json: [], 
    		vm_data: [], 
    		expire: null, 
    		description: "", 
    		envConfTestEnv: false, 
    		engineeringBuild: false, 
    		advancedEnabled: false};

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
    
    $scope.ready = function() {
      return $scope.settings.environmentName 
        && $scope.currentUser 
        && $scope.currentUser.authenticated 
        && $scope.settings.applicationName
        && $scope.settings.applicationServerType;
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


    var now = new Date();
    $scope.expireDateOptions = {
      'min': new Date().setDate(now.getDate() + 1), // Tomorrow
      'max': new Date().setDate(now.getDate() + 365 * 10), // in 10 years..
      'starting-day': "1" // Monday
    };

    $scope.prepSave = function(statusText) {
        $scope.status = statusText;
        $scope.order.vm_data = $scope.order.vm_data_json;
    };

    $scope.submitOrder = function() {
        $scope.prepSave('Bestillt');
        $scope.order.status = 'Q';
        $http.post("rest/orders?dryRun=true", $scope.settings).success(function() {
        	alert("Yeah!");
        });
    };

    $scope.addServer = function(via) {
      var via = via || '';
      var size = $scope.serverSize || 's';
      $scope.order.vm_data_json.push({guestos: 'rhel60', size: size, type: 'ap', dmz: false, description: '', puppetFact: [], disk: []});
      if(via === 'button') {
        $scope.serverCountDropdown = '';
      }
    };

    $scope.$watch("settings.serverSize", function(newVal, oldVal) {
      if(oldVal == undefined || oldVal == newVal) { return; }
      $scope.order.vm_data_json.forEach(function(server) {
        server.size = newVal;
      });
    });

    $scope.$watch("settings.serverCount", function(newVal, oldVal) {
      if(newVal == undefined || oldVal == newVal || newVal == '') { return; }
      //var currentCount = $scope.order.vm_data_json.length;  // Shall we have logic to handle already existing servers?
      var currentCount = 0;
      $scope.order.vm_data_json = [];
      for(var i=currentCount; i<newVal; i++) {
        $scope.addServer();
      }
    });

    // Validation logic
    $scope.$watch(
      function($scope) { return JSON.stringify($scope.order); },
      function() {
        // Everything here will be called for every change in $scope.order, every keypress.. Keep it clean.
        // Also note that tekst pushed to the noSaveErrors array will not render html entities as characters,
        // Use of octal is deprecated, so just use the characters.. :(
        $scope.noSaveErrors = Array();

        if($scope.order.vm_data_json !== undefined && $scope.order.vm_data_json.length == 0) {
          $scope.noSaveErrors.push('Minst 1 server må defineres.');
        }

        if($scope.noSaveErrors.length == 0) {
          $scope.saveDisabled = false;
        } else {
          $scope.saveDisabled = true;
        }
      }
    );


  }]);
