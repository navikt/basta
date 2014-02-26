'use strict';

angular.module('skyBestApp.order_list_controller', [])
  .controller('orderListController', ['$scope', '$http', '$resource', '$location', '$timeout', function($scope, $http, $resource, $location, $timeout) {

    function cancelOrderUpdate() {
      if ($scope.orderUpdate) {
        $timeout.cancel($scope.orderUpdate);
        delete $scope.orderUpdate;
      }
    }
    
    $scope.$on('$destroy', function() {
      cancelOrderUpdate();
    });
    
    $scope.isSelectedOrder = function(order) { 
      return order.id === $scope.selectedOrderId; 
    };
    $scope.setSelectedOrder = function(order) {
      if (!order) {
        return;
      }
      function updateOnInterval(order) {
        $http.get(order.uri).success(function(data) {
          $scope.orderDetails = data;
          $scope.orders = _($scope.orders).map(function(order) { 
            if (order.id === data.id) {
              return data;  
            }
            return order;
          });
        });
        $scope.orderUpdate = $timeout(function() { updateOnInterval(order); }, 10000);
      }
      if ($scope.selectedOrderId !== order.id) {
        cancelOrderUpdate();
        $scope.selectedOrderId = order.id;
        $scope.orderDetails = order;
        updateOnInterval(order);
      }
    };
    
    function getOrderById(orderId) {
      return _.find($scope.orders, function(order) { return order.id === orderId; });
    }
    
    var Orders = $resource('/rest/orders/:identifier');
    Orders.query(function(orders) {
      $scope.orders = orders;
      var orderId = ($location.search()).id;
      if (orderId) {
        var order = getOrderById(parseInt(orderId));
        if (order) {
          $scope.setSelectedOrder(order);
        }
      }
    });

  }]);