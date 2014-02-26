'use strict';

angular.module('skyBestApp.order_list_controller', [])
  .controller('orderListController', ['$scope', '$http', '$resource', '$location', function($scope, $http, $resource, $location) {

    function loadAdditionalData(property, uriProperty, toggle) {
      return function(object) {
        if (toggle && !_.isUndefined(object[property])) {
          delete object[property];
        } else {
          $http.get(object[uriProperty]).success(function(data){
            object[property] = data;
          });
        }
      };
    };

    $scope.isSelectedOrder = function(order) { 
      return order.id === $scope.selectedOrderId; 
    };
    $scope.setSelectedOrder = function(order) {
      function updateOnInterval(order) {
        if ($scope.selectedOrderId === order.id) {
          loadAdditionalData('nodes', 'nodesUri')(order);
          setTimeout(function() { updateOnInterval(order); }, 10000);
        }
      }
      if ($scope.selectedOrderId !== order.id) {
        $scope.selectedOrderId = order.id;
        $scope.orderDetails = order;
        updateOnInterval(order);
      }
    };
    $scope.toggleRequestXml = loadAdditionalData('requestXml', 'requestXmlUri', true); 
    $scope.toggleOrderDetailsSettings = loadAdditionalData('settings', 'settingsUri', true); 
    
    var Orders = $resource('/rest/orders/:identifier');
    Orders.query(function(orders) {
      $scope.orders = orders;
      var orderId = ($location.search()).id;
      if (orderId) {
        $scope.setSelectedOrder(_.find($scope.orders, function(order) { return order.id === orderId; }));
      }
    });

  }]);