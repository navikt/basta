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


        $scope.ModalController = function ($scope) {

            $scope.actions = {
                START: {
                    'header': 'Start',
                    'message': 'Er du sikker på at du ønsker å starte ',
                    'url': 'rest/vm/nodes/start'

                },
                STOP: {
                    'header': 'Stopp',
                    'message': 'Er du sikker på at du ønsker å stoppe ',
                    'url': 'rest/vm/nodes/stop'

                },
                DELETE: {
                    'header': 'Avbestill',
                    'message': 'Er du sikker på at du ønsker å avbestille ',
                    'url': 'rest/vm/nodes/decommission'

                }
            }


            $scope.$watch('decommisionTarget', function () {
                $scope.target = $scope.decommisionTarget;
            });

            $scope.$watch('operation', function (newVal) {
                if (!_.isUndefined(newVal)) {
                    $scope.header = $scope.actions[$scope.operation].header;
                    $scope.message = $scope.actions[$scope.operation].message + " " + $scope.target + "?";
                    $scope.url = $scope.actions[$scope.operation].url;
                }


            });

            $scope.ok = function () {
                $("#modal").modal('hide').on('hidden.bs.modal', function () {
                    $http.post($scope.url, $scope.decommisionTarget.split(',')).success(function (result) {
                        $location.path('/order_details/' + result.orderId);
                    }).error(errorService.handleHttpError($scope.header, 'orderSend'));
                });
            };
        };
    }];
