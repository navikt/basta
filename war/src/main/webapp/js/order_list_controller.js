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

        $scope.sliderbusy = true;

        var page = 0;
        var size = 60;
        $scope.orders =[];
        queryOrder(page);

        function queryOrder(page) {
            OrderResource.query({page: page, size: size, fromdate: ($scope.slider.rangemin), todate: ($scope.slider.rangemax)}).
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

        $scope.$watch('slider.rangemax', function (newVal, oldVal) {
            if (newVal === oldVal || !_.isNumber(newVal)) {
                return;
            }
            $scope.slider.busy=true;
            delete $scope.orders;
            $scope.orders =[];
            queryOrder(0);
            console.log(newVal + " --> " + moment(newVal).toJSON());
        });

        $scope.$watch('slider.rangemin', function (newVal, oldVal) {
            if (newVal === oldVal || !_.isNumber(newVal)) {
                return;
            }

            $scope.slider.busy=true;
            delete $scope.orders;
            $scope.orders =[];
            queryOrder(0);
            console.log(newVal + " --> " + moment(newVal).toJSON());
        });



    }]);