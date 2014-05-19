'use strict';

angular.module('skyBestApp.order_details_controller', [])
    .controller('orderDetailsController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$timeout', '$rootScope',
        function ($scope, $http, $resource, $routeParams, $location, $timeout, $rootScope) {

            $scope.model = {
                exists: false,
                showXML: false,
                routeParamsId: $routeParams.id
            }

            $scope.selectedNode = null;

            var OrderResource = $resource('rest/orders/:orderId', {orderId: '@id'});
            var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId: '@id'});

            OrderResource.get({orderId: $routeParams.id})
                .$promise.then(
                function (value) {
                    $scope.model.exists = true;
                    $scope.orderDetails = value;
                },
                function (error) {
                    $scope.model.exists = false;
                }
            )
            $scope.statusLog = OrderLogs.query({orderId: $routeParams.id});

            $scope.filterDate = function(item){
                if ($scope.searchDate){
                    var d = moment(item.created).format('YYYY-MM-DD HH:mm:ss');
                    return (d).indexOf($scope.searchDate) !=-1;
                }
                return true;
            }

            $scope.setSelectedNode = function (node) {
                console.log(node.hostname);
                    $scope.selectedNode = node;
            };

            $scope.ModalController = function ($scope) {
                $scope.header = 'Dekommisjonering';
                $scope.$watch('selectedNode', function () {
                    if ($scope.selectedNode){
                        $scope.message = 'Er du sikker p� at du �nsker � dekommisjonere ' +  $scope.selectedNode.hostname + '?';
                    }
                });

                $scope.ok = function () {
                    $("#modal").modal('hide').on('hidden.bs.modal', function () {
                        $http.post('rest/orders', {nodeType: 'DECOMMISSIONING', hostnames: [$scope.selectedNode.hostname]}).success(function (order) {
                            $location.path('/order_list').search({ id: order.id });
                        }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));
                    });
                };
            };

        }]);