'use strict';

angular.module('skyBestApp.main_controller', [])
    .controller('mainController', ['$scope', '$rootScope','$http', '$templateCache', '$location', '$resource', function ($scope, $rootScope,  $http, $templateCache, $location, $resource) {
        function errorHandler(args) {
            $scope.$broadcast('GeneralError', {name: 'Autentiseringsfeil', httpError: args});
        }

        function retrieveUser() {
            $resource('/rest/users/:identifier').get({identifier: "current"}, function (data) {
                $scope.$broadcast('GeneralError', {removeName: 'Autentiseringsfeil'});
                $scope.currentUser = data;
            }, errorHandler);
        }

        $http({ method: 'GET', url: 'rest/datasource'}).success(function (data) {
            $scope.datasource = data;
        })

        function isAlive(){
            $http({ method: 'GET', url: 'rest/datasource/alive'}).success(function (data) {
                $rootScope.alive = data.dbAlive;
                if ($rootScope.alive) {
                    $scope.$broadcast('GeneralError', {removeName: 'Mangler kontakt med DB'});
                } else {
                    $scope.$broadcast('GeneralError', {name: 'Mangler kontakt med DB', message:'Ingen bestillinger kan gjennomføres'});
                }
            });
        }


        $scope.createOrder = function () {
            $location.path('/order');
        };
        $scope.showLogin = function () {
            $scope.userForm = {};
            $
        };
        $scope.login = function () {
            var config = { headers: { 'Content-Type': 'application/x-www-form-urlencoded' }};
            var data = $.param({ j_username: $scope.userForm.username, j_password: $scope.userForm.password });

            $http.post('security-check', data, config).success(function (data, status, headers, config) {
                if (data === 'success') {
                    $scope.$broadcast('GeneralError', {removeName: 'Autentiseringsfeil'});
                    delete $scope.userForm;
                    $scope.$broadcast("UserChanged");
                } else {
                    $scope.$broadcast('GeneralError', {name: 'Autentiseringsfeil', message: 'Innlogging feilet'});
                }
            }).error(errorHandler);
        };
        $scope.logout = function () {
            $http.get('logout').error(errorHandler);
            $scope.$broadcast('UserChanged');
        };

        $scope.createTemplate = function () {
            $location.path('/template');
        };

        function retrieveUserOnInterval() {
            retrieveUser();
            isAlive();
            setTimeout(retrieveUserOnInterval, 10000);
            setTimeout(isAlive, 10000);

        }


        retrieveUserOnInterval();
        $scope.$on('UserChanged', retrieveUser);
        $scope.location = $location;
    }]);

