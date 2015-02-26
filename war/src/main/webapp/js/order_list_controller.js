'use strict';

angular.module('basta.order_list_controller', [])
    .controller('orderListController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$timeout', '$rootScope',
        function ($scope, $http, $resource, $routeParams, $location, $timeout, $rootScope) {

            $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});

            var OrderResource = $resource('rest/orders/page/:page/:size/:fromdate/:todate', {page: '@page', size: '@size', fromdate: '@fromdate', todate: '@todate'});

            $scope.timespan = {
                values: [
                    {'description': 'Last 30 days', 'date': moment().subtract('months', 1).format('YYYY-MM-DD')},
                    {'description': 'Last 60 days', 'date': moment().subtract('months', 2).format('YYYY-MM-DD')},
                    {'description': 'Last 6 months', 'date': moment().subtract('months', 6).format('YYYY-MM-DD')},
                    {'description': 'Last year', 'date': moment().subtract('years', 1).format('YYYY-MM-DD')},
                    {'description': 'All time', 'date': moment('2013-01-01').format('YYYY-MM-DD')}
                ],
                selected: '',
                busy: true
            };

            $scope.timespan.selected = $scope.timespan.values[4];

            var page = 0;
            var size = 100;
            $scope.ordersSize = 0;

            $scope.ordersArray = [];
            $scope.orders = [];

            if ($routeParams.result && !_.isEmpty($routeParams.result)) {
                $scope.search = {
                    orderresults: $routeParams.result
                };

                $scope.timespan.selected = $scope.timespan.values[4];
            }

            function queryOrder(page) {
                $scope.timespan.busy = true;
                OrderResource.query({page: page, size: size, todate: moment().add('days', 1).startOf('day').valueOf(), fromdate: moment('2013-01-01').valueOf()}).
                    $promise.then(
                    function (orders) {
                        if (_.isEmpty(orders)) {
                            $scope.timespan.busy = false;
                            return;
                        }
                        _.map(orders, function (order) {
                            if (order.createdByDisplayName) {
                                order.createdByDisplayName = order.createdByDisplayName + " (" + order.createdBy + ")";
                            } else {
                                order.createdByDisplayName = order.createdBy;
                            }


                            function getType(order) {
                                if (_.isEmpty(order.orderOperation)) {
                                    return  _(order.orderOperation).humanize();
                                }
                                return _(order.orderOperation).humanize() + " | " +
                                        _(order.orderType).humanize() + " | " +
                                    _(order.orderDescription).chain().humanize().titleize().value();
                            }

                            order.type = getType(order);
                            order.orderresults = order.results.join();

                            $scope.ordersArray.push(order);
                            if ($scope.orders.length < size){
                                $scope.orders.push(order);
                            }
                        });
                        page++;
                        queryOrder(page);
                    },
                    function (error) {
                        console.log(error);
                    }
                );
            }

            queryOrder(page);


            $scope.loadMore = function (){
                if($scope.ordersArray.length + 1 > $scope.ordersSize){
                    $scope.ordersSize= $scope.ordersSize + size;
                }
                console.log($scope.ordersSize);
            }



            $scope.filterDate = function (item) {
                if ($scope.searchDate) {
                    var d = moment(item.created).format('YYYY-MM-DD HH:mm:ss');
                    return (d).indexOf($scope.searchDate) != -1;
                }
                return true;
            };


            $scope.$watch('timespan.selected', function (newVal, oldVal) {
                if (newVal.date === oldVal.date) {
                    return;
                }
                $scope.orders = [];
                queryOrder(0);
            });
        }]);

