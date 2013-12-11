'use strict';

angular.module('skyBestApp.main_controller', [])
  .controller('mainController', ['$scope', '$http', '$templateCache', '$location', '$resource', function($scope, $http, $templateCache, $location, $resource) {
    // TODO duplicated from order_form_controller.js
    function getField(object, fields) {
      if (object == null || fields.length == 0) 
        return object;
      else {
        var name = fields[0];
        fields.shift();
        return getField(object[name], fields);
      }
    }
    function errorHandler(data, status, headers, config) {
      var message = 'Feil oppstått! Http-kode ' + status;
      var detailedMessage = getField(data, ['html', 'head', 'title']);
      if (detailedMessage) {
        message += ' melding "' + detailedMessage + '"';
      }
      $scope.userForm.error = message;
    }
    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: "current"}, function(data) {
        $scope.currentUser = data;
      }, errorHandler);
    }

    $scope.userForm = {};

    $scope.clearCache = function() {
        $templateCache.removeAll();
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
      }).error(errorHandler);
    };
    $scope.logout = function() {
      $http.get('logout').error(errorHandler);
      $scope.$broadcast("UserChanged");
    };

    $scope.createTemplate = function() {
      $location.path('/template');
    };
    
    retrieveUser();
    $scope.$on("UserChanged", retrieveUser);
    $scope.location = $location;
  }]);

