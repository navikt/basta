'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", function($scope, User, BastaService) {

    this.choices = {
	memory : [ 2, 3, 4, 6, 8, 10, 12, 14, 16 ],
	serverCount : [ 1, 2, 4, 6, 8, 10 ]
    }

    this.settings = {
	classification : {
	    type : 'standard'
	}
    }

    this.data = {
	nodeType : 'JBOSS',
	environmentClass : 'u',
	zone : 'fss',
	applicationMappingName : null,
	environmentName : null,
	cpuCount : 2,
	serverCount : 1,
	memory : 2,
	extraDisk : null,
	classification : null

    }

    var vm = this;

  

    function isMultiSite() {
	var multiSites = [ 'q0', 'q1', 'q3', 'p' ];
	return _.contains(multiSites, vm.data.environmentName);
    }
  
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
	this.data.classification = vm.settings.classification.type;
	this.data.description = vm.settings.classification.description;
	console.log("creating new jboss order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/jboss', this.data);
    };

} ];
