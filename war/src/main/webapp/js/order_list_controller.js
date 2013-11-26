'use strict';

angular.module('skyBestApp.order_list_controller', [])
  .controller('orderListController', ['$scope', '$http', '$resource', '$location', function($scope, $http, $resource, $location) {

    $scope.isSelectedOrder = function(order) { 
      return order.id == $scope.selectedOrderId; 
    };
    $scope.setSelectedOrder = function(order) { 
      $scope.selectedOrderId = order.id;
      $scope.orderDetails = order;
    };
    $scope.toggleRequestXml = function(order) {
      if (order.requestXml) {
        delete order.requestXml;
      } else {
        $http.get(order.requestXmlUri).success(function(data){
          console.log(data);
          order.requestXml = data;
        });
      }
    };
    
    var Orders = $resource('/rest/orders/:identifier');
    Orders.query(function(orders) {
      $scope.orders = orders;
      var orderId = ($location.search()).id;
      if (orderId) {
        $scope.setSelectedOrder(_.find($scope.orders, function(order) { return order.id == orderId; }));
      }
    });
    
  }]);