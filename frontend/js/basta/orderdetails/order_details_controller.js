'use strict';
var moment = require('moment');
module.exports = ['$scope', '$http', '$resource', '$routeParams', '$location', '$interval', '$rootScope', '$timeout', 'errorService', 'accessChecker','notificationService',
        function ($scope, $http, $resource, $routeParams, $location, $interval, $rootScope, $timeout, errorService, $accessChecker, notificationService) {

            $scope.model = {
                exists: false,
                showXML: false,
                routeParamsId: $routeParams.id
            }

            $scope.model.notes =  notificationService.query();

            $scope.selectedNodes = null;

            var OrderResource = $resource('rest/orders/:orderId', {orderId: '@id'});
            var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId: '@id'});

            $scope.hasEnvironmentClassAccess = function () {
                return $accessChecker.hasEnvironmentClassAccess($scope, $scope.orderDetails.input.environmentClass);
            };

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
                            function getType(order) {
                                if (_.isEmpty(order.orderDescription)) {
                                    return  _(order.orderOperation).humanize();
                                }
                                return  _(order.orderOperation).humanize() + " " + _(order.orderType).humanize() + " of type " + _(order.orderDescription).chain().humanize().titleize().value();
                            }

                            function getOrderOperation(order) {

                                return  _(order.orderOperation).chain().humanize().titleize().value();
                            }

                            $scope.orderDetails.type = getType(value);
                            $scope.orderDetails.orderTypeHumanized = getOrderOperation(value);
                            $scope.model.existingNodes = nodesWithStatus('DECOMMISSIONED', true);
                            $scope.model.startedNodes = nodesWithStatus('ACTIVE');
                            $scope.model.stoppedNodes = nodesWithStatus('STOPPED');


                            function shouldStartPollAutomatically() {
                                var iscreatedLessThan40minutesAgo = moment().subtract(40, 'minutes').isBefore(moment(value.created));
                                var statusInProgress = ( $scope.orderDetails.status === 'PROCESSING' || $scope.orderDetails.status === 'NEW');
                                return iscreatedLessThan40minutesAgo && statusInProgress && $scope.polling === false;
                            }

                            if (shouldStartPollAutomatically()) {
                                $scope.startPoll();
                                $scope.automaticallyStarted = true;
                            }

                            function shouldStopPollAutomatically() {
                                return $scope.automaticallyStarted &&
                                    ($scope.orderDetails.status === 'SUCCESS' || $scope.orderDetails.status === 'ERROR');

                            }

                            if (shouldStopPollAutomatically()) {
                                $scope.automaticallyStarted = false;
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

            $scope.$on("timer-stopped", function () {
                $scope.polling = false;
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

            $scope.addAllNodes = function () {
                $scope.selectedNodes = _.chain($scope.orderDetails.nodes)
                    .filter(function (node) {
                        return _.isEmpty(node.decommissionOrder)
                    })
                    .map(function (node) {
                        return node.hostname;
                    })
                    .value();
            }

            $scope.selectNodes = function (nodes) {
                $scope.selectedNodes = nodes;
            }



            function nodesWithStatus(status, inverse) {
                var x = _.chain($scope.orderDetails.resultDetails)
                    .filter(function (node) {
                        if (inverse) {
                            return node.details.nodeStatus != status;
                        } else {
                            return node.details.nodeStatus === status
                        }
                    })
                    .map(function (node) {
                        return node.resultName;
                    })
                    .value();
                return x;

            }

            $scope.setSelectedNode = function (node) {
                $scope.selectedNodes = [node.resultName];
            };

            $scope.ModalController = ['$scope', function ($scope) {

                $scope.actions = {
                    START: {
                        'header': 'Start',
                        'message': 'Do you really want to start ',
                        'url': 'rest/vm/nodes/start'
                    },
                    STOP: {
                        'header': 'Stop',
                        'message': 'Do you really want to stop ',
                        'url': 'rest/vm/nodes/stop'
                    },
                    DELETE: {
                        'header': 'Decommission',
                        'message': 'Do you really want to decommission  ',
                        'url': 'rest/vm/nodes/decommission'
                    }
                };

                $scope.$watch('model.nodetarget', function (newVal) {
                    if (!_.isUndefined(newVal)) {
                        $scope.selectedNodes = newVal;
                    }
                });

                $scope.$watch('model.operation', function (newVal) {
                    if (!_.isUndefined(newVal)) {

                        $scope.header = $scope.actions[$scope.model.operation].header;
                        $scope.message = $scope.actions[$scope.model.operation].message + " " + $scope.selectedNodes + "?";
                        $scope.url = $scope.actions[$scope.model.operation].url;
                    }

                });

                $scope.ok = function () {
                    $("#modal").modal('hide').on('hidden.bs.modal', function () {
                        $http.post($scope.url, $scope.selectedNodes).success(function (result) {
                            $location.path('/order_details/' + result.orderId);
                        }).error(errorService.handleHttpError($scope.header, 'orderSend'));
                    });
                };
            }];


         /*   setTimeout(function () {
                $('.nodeinfo').tooltip({
                    html: true,
                    title: function () {
                        var content = $(this).next('.tooltip-content');
                        return content.html();
                    }
                });
            }, 500);*/


            $scope.copyOrder = function () {
                $location.path('/vm_order').search({id: $routeParams.id});
            }

        }];