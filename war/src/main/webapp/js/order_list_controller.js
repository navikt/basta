'use strict';

angular.module('skyBestApp.order_list_controller', [])
    .controller('orderListController', ['$scope', '$http', '$resource', '$location', '$timeout', '$rootScope', function ($scope, $http, $resource, $location, $timeout, $rootScope) {

        $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});

        var OrderResource = $resource('rest/orders/sub/:first/:size', {first: '@first', size: '@size'});

        var start = 0;
        var size = 30;
        $scope.orders = new Array();
        queryOrder(start);

        function queryOrder(start) {
            OrderResource.query({first: start, size: size}).
                $promise.then(
                function (orders) {
                    if(_.isEmpty(orders)){
                        console.log("done")
                        return;
                    }
                    _.map(orders, function (order) {
                        if (order.createdByDisplayName) {
                            order.createdByDisplayName = order.createdByDisplayName + " (" + order.createdBy + ")";
                        } else {
                            order.createdByDisplayName = order.createdBy;
                        }
                        $scope.orders.push(order);
                    });
                    console.log(orders.length + " vs " + size);
                    if(orders.length === size){
                        start = start + size;
                        size=  3 * size;
                        queryOrder(start);
                        console.log($scope.orders.length);
                    }



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