'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", "$http", "errorService", function($scope, User, BastaService, $http, errorService) {

    this.choices = {
	memory : [ 2048, 4096 ],
	serverCount : [ 1, 2, 4 ]
    }
    
    this.validation={
	fasitPrerequisite:false,
	fasitDetails: []
	
    };
    
    this.settings={
	    classification: {type: 'standard'}
    }

    this.data = {
	nodeType : 'BPM_DEPLOYMENT_MANAGER',
	environmentClass : 'u',
	zone : 'fss',
	environmentName : null,
	cpuCount : 2,
	memory : 2048,
	extradisk:20
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
	$http.get('rest/vm/orders/bpm/dmgr/validation', {
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
	
	return unitCost*2;
    }

    this.submitOrder = function() {
	this.data.classification=vm.settings.classification.type;
	this.data.description=vm.settings.classification.description;
	console.log("creating new bpm dmgr order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/bpm/dmgr', this.data);
    };

} ];
