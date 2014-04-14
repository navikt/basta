'use strict';

angular.module('skyBestApp.order_details_controller', [])
  .controller('orderDetailsController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$timeout','$rootScope',
        function($scope, $http, $resource, $routeParams, $location, $timeout,$rootScope) {

        var OrderResource  = $resource('rest/orders/:orderId', {orderId : '@id'});
        $scope.orderDetails = OrderResource.get({orderId:$routeParams.id});


        var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId : '@id'});

        $scope.statusLog = OrderLogs.get({orderId:$routeParams.id});
            console.log($scope.statusLog);

  }]);