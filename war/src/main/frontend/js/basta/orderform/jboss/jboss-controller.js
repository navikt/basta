'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", function($scope, User, BastaService) {

    this.data = {
	nodeType : 'JBOSS',
	environmentClass : 'u',
	zone : 'fss',
	applicationMappingName: null,
	environmentName: null,
	cpuCount : 1,
	serverCount : 1,
	memory : 1024
    }

   
    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	$scope.form.$setPristine();
    }
    
    this.submitOrder = function() {
	console.log("creating new jboss order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/jboss', this.data);
};

} ];
