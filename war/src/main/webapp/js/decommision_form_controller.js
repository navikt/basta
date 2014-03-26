'use strict';

angular.module('skyBestApp.decommision_form_controller', [])
    .controller('decommisionFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', 'errorService', function ($scope, $rootScope, $http, $routeParams, $resource, $location, errorService) {

        $scope.decommission = function () {
            $('#modal').modal('show');
        };

        $scope.ModalController = function ($scope) {

            $scope.$watch('decommisionTarget', function() {
                $scope.message = 'Er du sikker på at du ønsker å dekommisjonere ' + $scope.decommisionTarget + '?';
            });

            $scope.header = 'Dekommisjonering';

            $scope.ok = function () {
                console.log("nå gjør vi no greier assa");
                $('#modal').modal('hide');
                $http.post('rest/orders', {nodeType: 'DECOMMISSIONING', hostnames: [$scope.decommisionTarget]}).success(function(order) {
                    $location.path('/order_list').search({ id: order.id });
                }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));

            };
            $scope.cancel = function () {
                $('#modal').modal('hide');
            };
        };
    }]);
