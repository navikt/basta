'use strict';

/* Controllers */

angular.module('skyBestApp.controllers', [])

  .controller('introController', ['$scope', '$location', '$resource', function($scope, $location, $resource) {
    $scope.createOrder = function() {
      var orderResource = $resource('/api/order');
      var order = orderResource.save(function() {
        $location.path('/' + order.id);
      })
    };

    $scope.createTemplate = function() {
      $location.path('/template');
    }
  }])


  .controller('orderFormController', ['$scope', '$routeParams', '$resource', '$location', '$templateCache', function($scope, $routeParams, $resource, $location, $templateCache) {
    $scope.orderID = $routeParams.id;
    var orderResource = $resource('/api/order/:orderID', {}, {'update': {method:'PUT'}});

    if($scope.orderID) {
      console.log('Ordering');
      $scope.templateEdit = false;
    } else {
      console.log('template editing');
      $scope.templateEdit = true;
      $scope.orderID = 1;  // Order with ID 1 is the base template order. If it doesnt exist, it will be created on the server automaticly when trying to get it.
      }


    $scope.formsDisabled = true;  // We need to do some loading first...
    $scope.status = 'Loading order...';



    $scope.order = orderResource.get({orderID: $scope.orderID}, function(data) {
      $scope.formsDisabled = false;
    })

    $scope.$watch('order.environmentClass', function(newVal, oldVal) {
        if(newVal == oldVal) { return; }
        if($scope.order.environmentClass == 'utv') {
            $scope.order.changeDeployUser = false;
            $scope.order.createApplication = true;
        } else {
            $scope.order.changeDeployUser = true;
            $scope.order.createApplication = false;
        }
    })

    var templateResource = $resource('/api/template');
    $scope.$watch('order.orderType + order.application', function(newVal, oldVal) {
      if($scope.order.orderType && $scope.order.application) {
        if(oldVal == undefined) {
          return; // We arent ready and probably got here in the loading process..
        }
        templateResource.get({type: $scope.order.orderType, application: $scope.order.application}, function(data) {
          $scope.usingTemplate = true;
          //angular.copy(data, $scope.order);
          angular.extend($scope.order, data);
        }, function() {
          $scope.usingTemplate = false;
        })
      }
    })

    $scope.$watch('order.orderType', function(newVal, oldVal) {
        if(newVal == oldVal) { return; }
        if($scope.order.orderType == 'applicationPlatform') {
            $scope.order.updateEnvConfig = true;
        } else {
            $scope.order.updateEnvConfig = false;
        }
    })

    var now = new Date();
    $scope.expireDateOptions = {
      'min': new Date().setDate(now.getDate() + 1), // Tomorrow
      'max': new Date().setDate(now.getDate() + 365 * 10), // in 10 years..
      'starting-day': "1" // Monday
    };

    $scope.prepSave = function(statusText) {
        $scope.status = statusText;
        $scope.order.vm_data = $scope.order.vm_data_json;
    }

    $scope.saveAndSubmit = function() {
        $scope.prepSave('Bestillt');
        $scope.formsDisabled = true;
        $scope.order.status = 'Q';
        $scope.order.$update({orderID: $scope.orderID});  // $scope.orderID, since the template could have overridden our order.id
    }

    $scope.save = function() {
        $scope.prepSave('Lagret');

        // We cant save as a template, we will hit template-constraints..
        if($scope.order.status == 'T') {
          $scope.order.status = 'I';
        };

        $scope.order.$update({orderID: $scope.orderID});
    }

    $scope.saveAsTemplate = function() {
      $scope.prepSave('Template laget/oppdatert');
      if($scope.usingTemplate) {
        $scope.order.$update({orderID: $scope.order.id});
      } else {
        orderResource.save($scope.order, function(data) {
          console.log(data);
          $scope.usingTemplate = true;
        });
      }
    }

    $scope.clearCache = function() {
        $templateCache.removeAll();
        console.log('Template cache cleared...');
    };

    $scope.addServer = function(via) {
      var via = via || '';
      $scope.order.vm_data_json.push({guestos: 'rhel60', size: 's', type: 'ap', dmz: false, description: '', puppetFact: [], disk: []})
      if(via === 'button') {
        $scope.serverCountDropdown = '';
      }
    }

    $scope.$watch("serverCountDropdown", function(newVal, oldVal) {
      if(newVal == undefined || oldVal == newVal) { return; }
      //var currentCount = $scope.order.vm_data_json.length;  // Shall we have logic to handle already existing servers?
      var currentCount = 0;
      $scope.order.vm_data_json = [];
      for(var i=currentCount; i<newVal; i++) {
        $scope.addServer();
      }
    })

    // Logic that clears non-valid options on certain circumstances.
    $scope.$watch("order.environmentClass != 'qa'", function(newVal, oldVal) {
      if(oldVal == undefined || oldVal == newVal) { return; }
      $scope.order.multisite = '';
    })
    $scope.$watch("order.orderType == 'applicationPlatform'", function(newVal, oldVal) {
      if(oldVal == undefined || oldVal == newVal) { return; }
      $scope.order.vm_type = '';
    })

    // Validation logic
    $scope.$watch(
      function($scope) { return JSON.stringify($scope.order) },
      function() {
        // Everything here will be called for every change in $scope.order, every keypress.. Keep it clean.
        // Also note that tekst pushed to the noSaveErrors array will not render html entities as characters,
        // Use of octal is deprecated, so just use the characters.. :(
        $scope.noSaveErrors = Array();

        if($scope.order.environmentClass === '') {
          $scope.noSaveErrors.push('Ingen miljø-klasse er valgt.');
        }

        if($scope.order.orderType === '') {
          $scope.noSaveErrors.push('Ingen type er valgt.');
        }

        if($scope.order.application === '') {
          $scope.noSaveErrors.push('Ingen applikasjon er valgt.');
        }

        if($scope.order.environmentID === '') {
          $scope.noSaveErrors.push('Ingen miljø ID er valgt.');
        }

        /* Defaulting to 'ap' if nothing
        if($scope.order.orderType == 'applicationPlatform' && $scope.order.vm_type === '') {
          $scope.noSaveErrors.push('Ingen type/mellomvare er valgt. (Må velges når typen er applikasjon-platform)');
        }*/

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
    )


  }])
