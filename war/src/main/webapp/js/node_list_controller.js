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
      console.log(JSON.stringify(filterParameters));
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
  
  	$scope.selectedNodeIds = [];
    $scope.isSelectedNode = function(node) {
      return _($scope.selectedNodeIds).contains(node.id);
    };
    $scope.setSelectedNode = function(node) {
      if (ctrlKeyDown) {
        if (!$scope.isSelectedNode(node)) {
          $scope.selectedNodeIds.push(node.id);
        } else {
          $scope.selectedNodeIds = _($scope.selectedNodeIds).without(node.id);
        }
      } else {
        $scope.selectedNodeIds = [node.id];
      }
    };
    
    _.chain($scope.filters).keys().each(function(filter) {
      $scope.$watch('filters.' + filter, function(newVal, oldVal) {
    	if (newVal == oldVal) return;
    	loadNodes();
      });
    });

  }]);