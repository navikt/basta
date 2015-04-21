'use strict';

module.exports = ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', 'errorService', function ($scope, $rootScope, $http, $routeParams, $resource, $location, errorService) {

        $scope.superUser = false;

        function retrieveUser() {
            $resource('/rest/users/:identifier').get({identifier: 'current'}, function (data) {
                $scope.currentUser = data;
            }).$promise.then(function () {
                    if (!$scope.currentUser.superUser) {
                        $location.path('/');
                    } else {
                        $scope.superUser = true;
                    }
                });
        }

        retrieveUser();

        $scope.$on('UserChanged', retrieveUser);
    }];
