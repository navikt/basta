'use strict';

angular.module('skyBestApp.order_list_controller', [])
    .controller('orderListController', ['$scope', '$http', '$resource', '$location', '$timeout', '$rootScope', function ($scope, $http, $resource, $location, $timeout, $rootScope) {

        $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});

        var OrderResource = $resource('rest/orders/page/:page/:size', {page: '@page', size: '@size'});

        var page = 0;
        var size = 60;
        $scope.orders =[];
        queryOrder(page);

        function queryOrder(page) {
            OrderResource.query({page: page, size: size}).
                $promise.then(
                function (orders) {
                    if(_.isEmpty(orders)){
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

    }]);