'use strict';

angular.module('basta.order_list_controller', [])
    .controller('orderListController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$timeout', '$rootScope',
        function ($scope, $http, $resource, $routeParams, $location, $timeout, $rootScope) {

            $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});

            var OrderResource = $resource('rest/orders/page/:page/:size/:fromdate/:todate', {page: '@page', size: '@size', fromdate: '@fromdate', todate: '@todate'});

            var page = 0;
            var size = 200;
            var renderSize = 30;

            $scope.ordersSize = 0;
            $scope.ordersArray = [];

            if ($routeParams.orderresults && !_.isEmpty($routeParams.orderresults)) {
                $scope.search = {
                    orderresults: $routeParams.orderresults
                };
            }

            $scope.filterByParam = function(field, value){
                $scope.search={};
                $scope.search[field] = value;
            };

            function queryOrder(page) {
                OrderResource.query({page: page, size: size, todate: moment().add('days', 1).startOf('day').valueOf(), fromdate: moment('2013-01-01').valueOf()}).
                    $promise.then(
                    function (orders) {
                        if (_.isEmpty(orders)) {
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
                                    return _(order.orderOperation).humanize();
                                }
                                return _(order.orderOperation).humanize() + " | " +
                                    _(order.orderType).humanize() + " | " +
                                    _(order.orderDescription).chain().humanize().titleize().value();
                            }

                            order.type = getType(order);
                            order.orderresults = order.results.join();

                            $scope.ordersArray.push(order);
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
                    $scope.ordersSize= $scope.ordersSize + renderSize;
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
        }]);

