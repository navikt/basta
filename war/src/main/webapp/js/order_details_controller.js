'use strict';

angular.module('skyBestApp.order_details_controller', [])
    .controller('orderDetailsController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$interval', '$rootScope',
        function ($scope, $http, $resource, $routeParams, $location, $interval, $rootScope) {

            $scope.model = {
                exists: false,
                showXML: false,
                activeNodes:false,
                routeParamsId: $routeParams.id
            }

            $scope.selectedNodes = null;

            var OrderResource = $resource('rest/orders/:orderId', {orderId: '@id'});
            var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId: '@id'});


            $scope.polling = false;
            var max = 1;
            refresh(0);
            function refresh(times) {
                if (times >= max) {
                    $scope.stopPoll();
                } else {
                    OrderLogs.query({orderId: $routeParams.id}).
                        $promise.then(
                        function (value) {
                            $scope.statusLog = value;
                        }
                    );
                    OrderResource.get({orderId: $routeParams.id})
                        .$promise.then(
                        function (value) {
                            $scope.model.exists = true;
                            $scope.orderDetails = value;
                            $scope.model.activeNodes = activeNodesPresent();
                        },
                        function (error) {
                            $scope.model.exists = false;
                        }
                    )
                }
            }
            max = 800;
            var sleep = 3000;
            $scope.from = (max * sleep) / 1000;
            var poll;
            $scope.startPoll = function () {
                refresh(0);
                if (angular.isDefined(poll)) return;
                poll = $interval(refresh, sleep, max + 1);
                $scope.$broadcast('timer-start');
                $scope.polling = true;
            }
            $scope.stopPoll = function () {
                $interval.cancel(poll);
                poll = undefined;
                $scope.$broadcast('timer-stop');
                $scope.$broadcast('timer-set-countdown', $scope.from);
                $scope.polling = false;
            }

            $scope.$on("timer-stopped", function() {
                $scope.polling=false;
            })

            $scope.$on('$destroy', function () {
                $scope.stopPoll();
            });

            $scope.filterDate = function (item) {
                if ($scope.searchDate) {
                    var d = moment(item.created).format('YYYY-MM-DD HH:mm:ss');
                    return (d).indexOf($scope.searchDate) != -1;
                }
                return true;
            }

            $scope.addAllNodes = function(){
                $scope.selectedNodes = _($scope.orderDetails.nodes).map(function (node){
                    return node.hostname;
                });
            }

            $scope.setSelectedNode = function (node) {
                $scope.selectedNodes =[node.hostname];
            };

            function activeNodesPresent(){
                if($scope.orderDetails.nodes){
                    return _($scope.orderDetails.nodes).some(function (node){
                        return _.isEmpty(node.decommissionOrder);
                    });
                }
                return false;
            }

            function prettyHostNames(){
                return _($scope.selectedNodes).map(function(hostname){
                   return hostname + ' ';
                });
            }

            $scope.ModalController = function ($scope) {
                $scope.header = 'Dekommisjonering';
                $scope.$watch('selectedNodes', function () {
                    if ($scope.selectedNodes) {
                        console.log(prettyHostNames());
                        $scope.message = 'Er du sikker p� at du �nsker � dekommisjonere ' + prettyHostNames() + '?';
                    }
                });

                $scope.ok = function () {
                    $("#modal").modal('hide').on('hidden.bs.modal', function () {
                        $http.post('rest/orders', {nodeType: 'DECOMMISSIONING', hostnames: $scope.selectedNodes}).success(function (order) {
                            $location.path('/order_list').search({ id: order.id });
                        }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));
                    });
                };
            };

        }]);