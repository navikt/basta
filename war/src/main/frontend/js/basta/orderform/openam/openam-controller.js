'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", function($scope, User, BastaService) {

    this.choices = {
	memory : [ 2, 3, 4, 6, 8, 10, 12, 14, 16 ],
	serverCount : [ 1, 2]
    }

    this.settings = {
	classification : {
	    type : 'standard'
	}
    }

    this.data = {
	nodeType : 'OPENAM12_SERVER',
	environmentClass : 'u',
	zone : 'sbs',
	applicationMappingName : null,
	environmentName : null,
	cpuCount : 2,
	serverCount : 1,
	memory : 2
    }

    var vm = this;

  
    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
    }
  
    this.changeEnvironment = function() {
    }

    this.estimatedPrice = function() {
	var unitCost = 600 + 138 + this.data.cpuCount * 100 + this.data.memory * 400 ;
	if (vm.data.extraDisk) {
	    unitCost = unitCost +  vm.data.extraDisk*32;
	}
	if (vm.settings.classification.type === 'custom') {
	    unitCost = unitCost * 2;
	}
	
	return this.data.serverCount * unitCost;
    }

    this.submitOrder = function() {
	console.log("creating new openam order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/openam', this.data);
    };

} ];
