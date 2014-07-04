'use strict';

angular.module('skyBestApp.order_list_controller', [])
    .controller('orderListController', ['$scope', '$http', '$resource', '$location', '$timeout', '$rootScope', function ($scope, $http, $resource, $location, $timeout, $rootScope) {

        $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});

        var OrderResource = $resource('rest/orders/page/:page/:size/:fromdate/:todate', {page: '@page', size: '@size', fromdate: '@fromdate', todate: '@todate'});

        $scope.slider = {
            min: moment("2014-01-01").valueOf(),
            max: moment().valueOf(),
            rangemin: moment().subtract('months',2).valueOf(),
            rangemax: moment().valueOf(),
            busy : true
        };

        $scope.timespan ={
            values : [
                        {'description':'Siste 30 dager','date':moment().subtract('months',1).format('YYYY-MM-DD')},
                        {'description':'Siste 60 dager','date':moment().subtract('months',2).format('YYYY-MM-DD')},
                        {'description':'Siste halvår','date':moment().subtract('months',6).format('YYYY-MM-DD')},
                        {'description':'Siste år','date':moment().subtract('years',1).format('YYYY-MM-DD')},
                        {'description':'All historikk','date':moment('2013-01-01').format('YYYY-MM-DD')}
                     ],
            selected:''
             };

        $scope.timespan.selected = $scope.timespan.values[0];

        var page = 0;
        var size = 50;
        $scope.orders =[];
        queryOrder(page);

        function queryOrder(page) {
            OrderResource.query({page: page, size: size, todate: moment().add('days', 1).startOf('day').valueOf(), fromdate: moment($scope.timespan.selected.date).valueOf()}).
                $promise.then(
                function (orders) {
                    if(_.isEmpty(orders)){
                        $scope.slider.busy=false;
                        return;
                    }
                    _.map(orders, function (order) {
                        if (order.createdByDisplayName) {
                            order.createdByDisplayName = order.createdByDisplayName + " (" + order.createdBy + ")";
                        } else {
                            order.createdByDisplayName = order.createdBy;
                        }
                        function hostnames(order) {
                            return  _(order.nodes).map(function (node){return node.hostname;}).join();
                        }

                        order.hostNames = hostnames(order);
                        $scope.orders.push(order);
                    });
                    page++;
                    queryOrder(page);
                },
                function (error) {
                    console.log(error);
                }
            );
        }

        $scope.filterDate = function (item) {
            if ($scope.searchDate) {
                var d = moment(item.created).format('YYYY-MM-DD HH:mm:ss');
                return (d).indexOf($scope.searchDate) != -1;
            }
            return true;
        }


        $scope.$watch('timespan.selected', function (newVal, oldVal) {
            if (newVal.date === oldVal.date) {
                return;
            }
            $scope.orders=[];
            queryOrder(0);
        });
    }]);

