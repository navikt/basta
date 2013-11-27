'use strict';

angular.module('skyBestApp.order_list_controller', [])
  .controller('orderListController', ['$scope', '$http', '$resource', '$location', function($scope, $http, $resource, $location) {

    function toggleOptionalData(property, uriProperty) {
      return function(object) {
        if (object[property]) {
          delete object[property];
        } else {
          $http.get(object[uriProperty]).success(function(data){
            object[property] = data;
          });
        }
      };
    };

    $scope.isSelectedOrder = function(order) { 
      return order.id == $scope.selectedOrderId; 
    };
    $scope.setSelectedOrder = function(order) { 
      $scope.selectedOrderId = order.id;
      $scope.orderDetails = order;
      if (!order.nodes) {
        toggleOptionalData('nodes', 'nodesUri')(order);
      }
    };
    $scope.toggleRequestXml = toggleOptionalData('requestXml', 'requestXmlUri'); 
    $scope.toggleOrderDetailsSettings = toggleOptionalData('settings', 'settingsUri'); 
    
    var Orders = $resource('/rest/orders/:identifier');
    Orders.query(function(orders) {
      $scope.orders = orders;
      var orderId = ($location.search()).id;
      if (orderId) {
        $scope.setSelectedOrder(_.find($scope.orders, function(order) { return order.id == orderId; }));
      }
    });
    
  }]);