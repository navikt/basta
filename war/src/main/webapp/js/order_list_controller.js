'use strict';

angular.module('skyBestApp.order_list_controller', [])
  .controller('orderListController', ['$scope', '$http', '$resource', '$location', function($scope, $http, $resource, $location) {

    function toggleOptionalData(property, uriProperty) {
      return function(object) {
        $http.get(object[uriProperty]).success(function(data){
          object[property] = data;
        });
      };
    };

    $scope.isSelectedOrder = function(order) { 
      return order.id == $scope.selectedOrderId; 
    };
    $scope.setSelectedOrder = function(order) {
      function updateOnInterval(order) {
        if ($scope.selectedOrderId == order.id) {
          toggleOptionalData('nodes', 'nodesUri')(order);
          setTimeout(function() { updateOnInterval(order); }, 10000);
        }
      }
      if ($scope.selectedOrderId != order.id) {
        $scope.selectedOrderId = order.id;
        $scope.orderDetails = order;
        updateOnInterval(order);
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