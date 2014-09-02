'use strict';

angular.module('skyBestApp.order_details_controller', [])
    .controller('orderDetailsController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$interval', '$rootScope','errorService',
        function ($scope, $http, $resource, $routeParams, $location, $interval, $rootScope, errorService) {

            $scope.model = {
                exists: false,
                showXML: false,
                activeNodesNumber:0,
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
                            var applicationMapping = value.settings.applicationMapping;

                            if(applicationMapping.mappingType === "APPLICATION_GROUP") {
                                value.settings.applicationGroup = applicationMapping.name;
                                value.settings.applicationsInGroup =  applicationMapping.applications.join(", ");
                            } else {
                                value.settings.application = applicationMapping.name;
                            }

                            delete value.settings.applicationMapping;
                            $scope.model.exists = true;
                            $scope.orderDetails = value;
                            function getType(order){
                                if (_.isEmpty(order.nodeType)){
                                    return  _(order.orderType).humanize();
                                }
                                return  _(order.orderType + " | "  + order.nodeType).chain().humanize().titleize().value();
                            }
                            $scope.orderDetails.type = getType(value);
                            $scope.model.activeNodesNumber = numberOfActiveNodes();

                            function shouldStartPollAutomatically() {
                                var iscreatedLessThan40minutesAgo = moment().subtract(40, 'minutes').isBefore(moment(value.created));
                                var statusInProgress = ( $scope.orderDetails.status === 'PROCESSING' ||  $scope.orderDetails.status ==='NEW');
                                return iscreatedLessThan40minutesAgo && statusInProgress && $scope.polling===false;
                            }

                            if (shouldStartPollAutomatically()){
                                $scope.startPoll();
                                $scope.automaticallyStarted=true;
                            }

                            function shouldStopPollAutomatically(){
                                return $scope.automaticallyStarted &&
                                    ($scope.orderDetails.status === 'SUCCESS' || $scope.orderDetails.status === 'ERROR');

                            }

                            if (shouldStopPollAutomatically()){
                                 $scope.automaticallyStarted=false;
                                 $scope.stopPoll();

                            }
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
                $scope.stopPolledCalled = true;
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
                $scope.selectedNodes = _.chain($scope.orderDetails.nodes)
                    .filter(function (node){return _.isEmpty(node.decommissionOrder)})
                    .map(function (node){return node.hostname;})
                    .value();
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

            function numberOfActiveNodes(){
                if($scope.orderDetails.nodes){
                    var x = _($scope.orderDetails.nodes).reduce(function(memo, node){
                       if (_.isEmpty(node.decommissionOrder)){
                           return memo + 1;
                       }
                        return memo;
                    },0);
                    return x;
                }
                return 0;
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
                        $scope.message = 'Er du sikker på at du ønsker å dekommisjonere ' + prettyHostNames() + '?';
                    }
                });

                $scope.ok = function () {
                    $("#modal").modal('hide').on('hidden.bs.modal', function () {
                        $http.post('rest/nodes/decommission', $scope.selectedNodes).success(function (result) {
                            $location.path('/order_details/'+ result.orderId);
                        }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));
                    });
                };
            };

            $scope.copyOrder = function () {
                $location.path('/order').search({id: $routeParams.id});
            }

        }]);