'use strict';

angular.module('basta.main_controller', [])
    .controller('mainController', ['$scope', '$rootScope', '$http', '$templateCache', '$location', '$resource','notificationService',  function ($scope, $rootScope, $http, $templateCache, $location, $resource, notificationService) {

        function handleAndDisplayRelevantVersionInfo() {
            $http.get('/version').then(function (data) {
                $scope.version = data.data;
                if (itIsANewVersionForUser($scope.version)) {
                    $http.get('/changelog/' + $scope.version + '.md').success(function (data) {
                        localStorage.setItem('version', $scope.version);
                        $location.url('changelog?version=' + $scope.version + "&firstTime");
                    });
                }
            });
        }

        var itIsANewVersionForUser = function (version) {
            var storedVersion = localStorage.getItem('version');
            return _.isUndefined(storedVersion) || storedVersion != version;
        };

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

        function isAlive() {
            $http({ method: 'GET', url: 'rest/datasource/alive'}).success(function (data) {
                $rootScope.alive = data.dbAlive;
                if ($rootScope.alive) {
                    $scope.$broadcast('GeneralError', {removeName: 'Mangler kontakt med DB'});
                } else {
                    $scope.$broadcast('GeneralError', {name: 'Mangler kontakt med DB', message: 'Ingen bestillinger kan gjennomføres'});
                }
            });
        }

        $scope.createOrder = function () {
            $location.path('/order');
        };

        $scope.showLogin = function () {
            $scope.userForm = {};
            setTimeout(function () {
                $('#login_username').focus();
            }, 200);
        };

        $scope.login = function () {
            var config = { headers: { 'Content-Type': 'application/x-www-form-urlencoded' }};
            var data = $.param({ j_username: $scope.userForm.username, j_password: $scope.userForm.password });

            $http.post('/security-check', data, config).success(function (data, status, headers, config) {
                if (data === 'success') {
                    $scope.$broadcast('GeneralError', {removeName: 'Autentiseringsfeil'});
                    delete $scope.userForm;
                    $scope.$broadcast("UserChanged");
                } else {
                    console.log(config);
                    $scope.$broadcast('GeneralError', {name: 'Autentiseringsfeil', message: 'Innlogging feilet'});
                }
            }).error(errorHandler);
        };

        $scope.logout = function () {
            $http.get('/logout').error(errorHandler);
            $scope.$broadcast('UserChanged');
        };

        $scope.createTemplate = function () {
            $location.path('/template');
        };

        function isBlocking(notifications){
            notifications.$promise.then(function(notes){
                $scope.isAnyBlockingNotifications = _.any(notes, function(note){
                    return note.blockOperations === true;
                });
            });
        }


        $scope.notifications = notificationService.query();
        isBlocking($scope.notifications);


        $scope.$on('notification:updated', function() {
            $scope.notifications = notificationService.query();
            isBlocking($scope.notifications);
        });


        function retrieveUserOnInterval() {
            retrieveUser();
            isAlive();
            setTimeout(retrieveUserOnInterval, 10000);
            setTimeout(isAlive, 10000);
        }

        retrieveUserOnInterval();
        $scope.$on('UserChanged', retrieveUser);
        $scope.location = $location;

        handleAndDisplayRelevantVersionInfo();
    }]);

