'use strict';

angular.module('skyBestApp.decommision_form_controller', [])
    .controller('decommisionFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', 'errorService', function ($scope, $rootScope, $http, $routeParams, $resource, $location, errorService) {

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

        $scope.ModalController = function ($scope) {
            $scope.header = 'Dekommisjonering';
            $scope.$watch('decommisionTarget', function () {
                $scope.message = 'Er du sikker på at du ønsker å dekommisjonere ' + $scope.decommisionTarget + '?';
            });

            $scope.ok = function () {
                $("#modal").modal('hide').on('hidden.bs.modal', function () {
                    $http.post('rest/orders', {nodeType: 'DECOMMISSIONING', hostnames: [$scope.decommisionTarget]}).success(function (order) {
                        $location.path('/order_list').search({ id: order.id });
                    }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));
                });
            };
        };
    }]);
