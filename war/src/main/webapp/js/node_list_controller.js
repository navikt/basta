'use strict';

angular.module('skyBestApp.node_list_controller', [])
  .controller('nodeListController', ['$scope', '$http', '$resource', function($scope, $http, $resource) {

    $scope.filters = {
      myNodes: true,
      activeNodes: true
    };
        
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
  
    $scope.isSelectedNode = function(node) {
      return $scope.selectedNodeId == node.id;
    };
    $scope.setSelectedNode = function(node) {
      $scope.selectedNodeId = node.id;
    };
    
    _.chain($scope.filters).keys().each(function(filter) {
      $scope.$watch('filters.' + filter, function(newVal, oldVal) {
    	if (newVal == oldVal) return;
    	loadNodes();
      });
    });

  }]);