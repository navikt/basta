'use strict';

var angular = require('angular');

module.exports = ["BastaService", "$http", "errorService",'$routeParams', function( BastaService, $http, errorService,$routeParams) {

    this.validation={
	fasitPrerequisite:false,
	fasitDetails: []
	
    };
    
    this.data = {
		nodeType : 'BPM_DEPLOYMENT_MANAGER',
		environmentClass : $routeParams.environmentClass || 'u',
		zone :  'fss',
		environmentName : $routeParams.environmentName,
		cpuCount : 2,
		memory : 4,
		serverCount: 1,
		extraDisk : 10
		}

    
    var vm= this;
    
    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	this.validation.fasitPrerequisite=false;
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
	console.log("creating new bpm dmgr order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/bpm/dmgr', this.data);
    };

} ];
