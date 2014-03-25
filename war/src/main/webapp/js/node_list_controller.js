'use strict';

angular.module('skyBestApp.node_list_controller', [])
  .controller('nodeListController', ['$scope', '$http', '$location', '$resource', '$modal', '$rootScope', 'errorService', function($scope, $http, $location, $resource, $modal, $rootScope, errorService) {

    function retrieveUser() {
      $resource('/rest/users/:identifier').get({identifier: 'current'}, function(data) {
        $scope.currentUser = data;
        loadNodes();
      });
    }
    retrieveUser();
    $scope.$on('UserChanged', retrieveUser);

    $scope.ModalController = function($scope) {
      $scope.header = 'Dekommisjonering';
      $scope.$watch('selectedNodes', function() {
        $scope.message = 'Er du sikker på at du ønsker å dekommisjonere disse serverne: ' + _($scope.selectedNodes).map(function(n) { return ' ' + n.hostname; } );
      });
      $scope.ok = function() {
        $http.post('rest/orders', {nodeType: 'DECOMMISSIONING', hostnames: _($scope.selectedNodes).pluck('hostname')}).success(function(order) {
          $location.path('/order_list').search({ id: order.id });
        }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));
        $('#modal').modal('hide');
      };
      $scope.cancel = function() {
        $('#modal').modal('hide');
      };
    };

    $scope.decommission = function() {
      $('#modal').modal('show');
    };
      
    $scope.filters = {
      myNodes: true,
      activeNodes: true
    };

    var ctrlKeyDown = false;  
    function keyevents(e) {
      ctrlKeyDown = e.ctrlKey;
    }
    window.onkeydown = keyevents;
    window.onkeyup = keyevents; 
    
    function loadNodes() {
      var filterParameters = { includeDecommissioned: ! $scope.filters.activeNodes };
      if ($scope.filters.myNodes && $scope.currentUser.authenticated) {
        filterParameters = _.extend(filterParameters, { user: $scope.currentUser.username });
      }
      $resource('/rest/nodes/:identifier', filterParameters).query(function(nodes) {
        $scope.nodes = nodes;
      });
    }
  
  	$scope.selectedNodes = [];
    $scope.isSelectedNode = function(node) {
      return _($scope.selectedNodes).find(function(o) { return node.id === o.id; });
    };
    $scope.setSelectedNode = function(node) {
      if (ctrlKeyDown) {
        if (!$scope.isSelectedNode(node)) {
          $scope.selectedNodes = $scope.selectedNodes.concat([node]);
        } else {
          $scope.selectedNodes = _($scope.selectedNodes).filter(function(o) { return node.id !== o.id; });
        }
      } else {
        $scope.selectedNodes = [node];
      }
    };
    
    _.chain($scope.filters).keys().each(function(filter) {
      $scope.$watch('filters.' + filter, function(newVal, oldVal) {
      	if (newVal === oldVal) return;
      	loadNodes();
      });
    });

  }]);