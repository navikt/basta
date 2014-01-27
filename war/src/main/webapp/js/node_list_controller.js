'use strict';

angular.module('skyBestApp.node_list_controller', [])
  .controller('nodeListController', ['$scope', '$http', '$resource', function($scope, $http, $resource) {

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
    function retrieveUser() {
  	  $resource('/rest/users/:identifier').get({identifier: 'current'}, function(data) {
  	    $scope.currentUser = data;
  	    loadNodes();
  	  });
    }
  	retrieveUser();
  	$scope.$on('UserChanged', retrieveUser);
  
  	$scope.selectedNodes = [];
    $scope.isSelectedNode = function(node) {
      return _($scope.selectedNodes).find(function(o) { return node.id == o.id; });
    };
    $scope.setSelectedNode = function(node) {
      if (ctrlKeyDown) {
        if (!$scope.isSelectedNode(node)) {
          $scope.selectedNodes.push(node);
        } else {
          $scope.selectedNodes = _($scope.selectedNodes).filter(function(o) { return node.id != o.id; });
        }
      } else {
        $scope.selectedNodes = [node];
      }
    };
    
    _.chain($scope.filters).keys().each(function(filter) {
      $scope.$watch('filters.' + filter, function(newVal, oldVal) {
    	if (newVal == oldVal) return;
    	loadNodes();
      });
    });

  }]);