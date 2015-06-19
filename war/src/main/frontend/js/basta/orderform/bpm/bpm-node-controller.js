'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", "$http", "errorService", function($scope, User, BastaService, $http, errorService) {

    this.choices = {
	memory : [ 512, 1024, 2048, 4096 ],
	serverCount : [ 1, 2, 4 ]
    }
    
    this.validation={
	fasitPrerequisite:false,
	fasitDetails: []
	
    };
    

    this.data = {
	nodeType : 'BPM_NODES',
	environmentClass : 'u',
	zone : 'fss',
	applicationMappingName : null,
	environmentName : null,
	cpuCount : 1,
	serverCount : 1,
	memory : 1024,
	extraDisk : 20,
    }
    
    var vm= this;
    

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	this.validation.fasitPrerequisite=false;
    }
    
    this.changeZone = function() {
	checkFasit();
    }
  
    this.changeEnvironment = function() {
	checkFasit();
    }
    

    function checkFasit() {
	$http.get('rest/vm/orders/bpm/node/validation', {
	    params : {
		environmentClass: vm.data.environmentClass,
		zone: vm.data.zone, 
		environmentName: vm.data.environmentName
	    }})
	.error(errorService.handleHttpError('Fasit sjekk om påkrevde ressurser eksisterer'))
	.success(function(data) {
	    vm.validation.fasitPrerequisite=!_.isEmpty(data);
	    vm.validation.fasitDetails=data;
	});
    };
    
    

    this.estimatedPrice = function() {
	var unitCost = 600 + 732 + this.data.cpuCount * 100 + this.data.memory * 0.4;
	return this.data.serverCount * unitCost *3.14;
    }

    this.submitOrder = function() {
	this.data.classification=vm.settings.classification.type;
	this.data.description=vm.settings.classification.description;
	console.log("creating new bpm node order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/bpm/node', this.data);
    };

} ];
