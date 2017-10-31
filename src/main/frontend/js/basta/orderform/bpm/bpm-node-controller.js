'use strict';

var angular = require('angular');

module.exports = [ "BastaService", "$http", "errorService", "FasitService", '$routeParams', function(BastaService, $http, errorService, FasitService, $routeParams) {

    this.choices = {
	memory : [ 8, 16 ],
	cpuCount : [ 2, 4 ], 
	serverCount_standard : [ 1, 2, 3, 4, 5, 6, 7, 8 ],
	serverCount_multisite : [ 2, 4, 6, 8, 10, 12, 14, 16 ]	
    }

    this.validation = {
	fasitPrerequisite : false,
	fasitDetails : []

    };

    this.data = {
	nodeType : 'BPM_NODES',
	environmentClass : $routeParams.environmentClass || 'u',
	zone : 'fss',
	applicationMappingName : null,
	environmentName : $routeParams.environmentName,
	cpuCount : $routeParams.cpuCount || 4,
	serverCount : $routeParams.serverCount || 1,
	memory : $routeParams.memory || 16,
	extraDisk : 10,
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

    function checkFasit() {
	$http.get('rest/vm/orders/bpm/node/validation', {
	    params : {
		environmentClass : vm.data.environmentClass,
		zone : vm.data.zone,
		environmentName : vm.data.environmentName
	    }
	}).error(errorService.handleHttpError('Fasit sjekk om påkrevde ressurser eksisterer')).success(function(data) {
	    vm.validation.fasitPrerequisite = !_.isEmpty(data);
	    vm.validation.fasitDetails = data;
	});
    }
    ;

    this.estimatedPrice = function() {
	var unitCost = 600 + 732 + this.data.cpuCount * 100 + this.data.memory * 0.4 ;
	return this.data.serverCount * unitCost * 3.14;
    }

    this.submitOrder = function() {
	console.log("creating new bpm node order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/bpm/node', this.data);
    };

} ];
