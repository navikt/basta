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
    		vm_count: 1, 
    		vm_data_json: [], 
    		vm_data: [], 
    		environmentClass: "utv", 
    		environmentID: "", 
    		application: "", 
    		vm_type: "", 
    		zone: "fss", 
    		owner: "", 
    		portfolio: "", 
    		project_id: "", 
    		role: "", 
    		multisite: false, 
    		expire: null, 
    		description: "", 
    		updateEnvConfig: false, 
    		changeDeployUser: false, 
    		envConfTestEnv: false, 
    		createApplication: false, 
    		engineeringBuild: false, 
    		advancedEnabled: false};

    $scope.settings = {
      serverCount: 1,
      serverSize: 's',
      disk: false 
    };
    	    
    $scope.choices = {
      environmentClasses: ['utv', 'test', 'qa', 'prod'],
      serverCounts: [1, 2, 4, 8],
      serverSizes: {s: 'Liten', m: 'Medium', l: 'Stor'},
      applicationServerTypes: {jb: 'Jboss', wa: 'WAS'}
    };
    
    function xml2json(data) {
      return new X2JS().xml_str2json(data);
    }
    
    $http({ method: 'GET', url: '/api/helper/fasit/environments', transformResponse: xml2json }).success(function(data) {
      $scope.choices.environments = _.groupBy(data.collection.environment, 'envClass');
    });
    $http({ method: 'GET', url: '/api/helper/fasit/applications', transformResponse: xml2json }).success(function(data) {
      $scope.choices.applications = data.collection.application;
    });

    $scope.$watch('order.environmentClass', function(newVal, oldVal) {
        if(newVal == oldVal) { return; }
        if($scope.order.environmentClass == 'utv') {
            $scope.order.changeDeployUser = false;
            $scope.order.createApplication = true;
        } else {
            $scope.order.changeDeployUser = true;
            $scope.order.createApplication = false;

            helperResource.get({type: 'fasit-environments', env_class: newVal}, function(data) {
              $scope.environments = data;
            }, function(err) { console.log(err); });
        }
    });


    $scope.$watch('order.vm_type', function(newVal, oldVal) {
        if(newVal == oldVal) { return; }
        if($scope.order.vm_type == 'wa') {
          var was_puppet_fact = 'cloud_application_dmgr';
          $scope.order.vm_data_json.forEach(function(server) {
            server.puppetFact.push({name:was_puppet_fact, value:'e34jbsl00995.devillo.no'});
          })
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

    $scope.saveAndSubmit = function() {
        $scope.prepSave('Bestillt');
        $scope.order.status = 'Q';
        $http.post("rest/environments/" + $scope.order.environmentClass + "/orders?verify=false", $scope.order).success(function() {
        	alert("Yeah!");
        }).error(function (data, status) {
        	alert("Doh " + status);
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

    // Logic that clears non-valid options on certain circumstances.
    $scope.$watch("order.environmentClass != 'qa'", function(newVal, oldVal) {
      if(oldVal == undefined || oldVal == newVal) { return; }
      $scope.order.multisite = '';
    });

    // Validation logic
    $scope.$watch(
      function($scope) { return JSON.stringify($scope.order); },
      function() {
        // Everything here will be called for every change in $scope.order, every keypress.. Keep it clean.
        // Also note that tekst pushed to the noSaveErrors array will not render html entities as characters,
        // Use of octal is deprecated, so just use the characters.. :(
        $scope.noSaveErrors = Array();

        if($scope.order.environmentClass === '') {
          $scope.noSaveErrors.push('Ingen miljø-klasse er valgt.');
        }

        if($scope.order.application === '') {
          $scope.noSaveErrors.push('Ingen applikasjon er valgt.');
        }

        if($scope.order.environmentID === '') {
          $scope.noSaveErrors.push('Ingen miljø ID er valgt.');
        }

        if(!RegExp(/^[a-zA-Z][0-9]{6}$/).test($scope.order.owner)) {
          $scope.noSaveErrors.push('Ingen eier definert (må matche formatet "x123456")');
        }

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
