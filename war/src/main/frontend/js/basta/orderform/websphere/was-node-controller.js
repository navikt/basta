'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", "$http", "errorService", "FasitService",'$routeParams', function($scope, User, BastaService, $http, errorService, FasitService, $routeParams) {

    this.choices = {
	memory : [ 2, 3, 4, 6, 8, 10, 12, 14, 16 ],
    	serverCount_standard : [ 1, 2, 3, 4, 5, 6, 7, 8 ],
	serverCount_multisite : [ 2, 4, 6, 8, 10, 12, 14, 16 ]	
    }

    this.validation = {
	fasitPrerequisite : false,
	fasitDetails : []

    };

    this.settings = {
	classification : {
	    type : 'standard'
	}
    }

    this.data = {
    	wasVersion : 'WAS8',
		nodeType : $routeParams.nodeType || 'WAS_NODES',
		environmentClass: $routeParams.environmentClass || 'u',
		zone: $routeParams.zone || 'fss',
		applicationMappingName: $routeParams.applicationMappingName || null,
		environmentName: $routeParams.environmentName || null,
		cpuCount: $routeParams.cpuCount || 2,
		serverCount: $routeParams.serverCount || 1,
		memory: $routeParams.memory || 2,
		extraDisk: $routeParams.extraDisk || null,
		classification: $routeParams.classification || null
    }

    this.choices.serverCount=this.choices.serverCount_standard;
    var vm = this;

    function isMultiSite() {
	return FasitService.isMultiSite(vm.data.environmentClass,vm.data.environmentName );
    }

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	this.validation.fasitPrerequisite = false;
    }

    this.changeZone = function() {
	checkFasit();
    }

    this.changeEnvironment = function() {
	checkFasit();
	if (isMultiSite()){
	    vm.choices.serverCount=vm.choices.serverCount_multisite;
	}else{
	    vm.choices.serverCount=vm.choices.serverCount_standard;
	}
	if (!_(vm.choices.serverCount).contains(vm.data.serverCount)){
	    console.log("Illegal choice for servercount. Clearing data")
	    delete vm.data.serverCount
	}
    }
    
    this.changeNodeType = function() {
        if (vm.data.wasVersion == "WAS9") {
        	vm.data.nodeType = 'WAS9_NODES';
        } else {
        	vm.data.nodeType = 'WAS_NODES';
        }
    	checkFasit();        
    }

    function checkFasit() {
	$http.get('rest/vm/orders/was/node/validation', {
	    params : {
		environmentClass : vm.data.environmentClass,
		zone : vm.data.zone,
		environmentName : vm.data.environmentName,
		nodeType : vm.data.nodeType
	    }
	}).error(errorService.handleHttpError('Fasit sjekk om påkrevde ressurser eksisterer')).success(function(data) {
	    vm.validation.fasitPrerequisite = !_.isEmpty(data);
	    vm.validation.fasitDetails = data;
	});
    }
    ;

    this.estimatedPrice = function() {
	var unitCost = 600 + 732 + this.data.cpuCount * 100 + this.data.memory * 400;
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
	console.log("creating new was node order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/was/node', this.data);
    };

} ];
