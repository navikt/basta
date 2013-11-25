'use strict';

angular.module('skyBestApp.order_list_controller', [])
  .controller('orderListController', ['$scope', '$http', '$resource', function($scope, $http, $resource) {
    var Orders = $resource('/rest/orders/:identifier');
    Orders.query(function(orders) {
      $scope.orders = orders;
    });
  }]);