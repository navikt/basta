'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", "$http", "errorService", function($scope, User, BastaService, $http, errorService) {

    this.choices = {
	memory : [ 512, 1024, 2048, 4096 ],
	serverCount : [ 1, 2, 4 ]
    }
    
    this.validation={
	dmgrMissing:false,
	wasAdminMissing:false,
	ldapUserMissing:false
    };
    
    this.settings={
	    classification: {type: 'standard'}
    }

    this.data = {
	nodeType : 'WAS_NODES',
	environmentClass : 'u',
	zone : 'fss',
	applicationMappingName : null,
	environmentName : null,
	cpuCount : 1,
	serverCount : 1,
	memory : 1024,
	extraDisk : null,
	classification: null
    }
    
    var vm= this;
    
    function isMultiSite(){
	var multiSites=['q0','q1', 'q3','p'];
	return _.contains(multiSites, vm.data.environmentName);
    }

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
    }
    
  
    this.changeEnvironment = function() {
	checkIfWasDmgrExistInFasit('DeploymentManager', 'wasDmgr');
	console.log(this.validation);
    }
    

    function checkIfWasDmgrExistInFasit(resourceType, alias) {
	$http.get('rest/vm/orders/was/existInFasit', {
	    params : {
		environmentClass: vm.data.environmentClass,
		zone: vm.data.zone, 
		environmentName: vm.data.environmentName,
		type:resourceType,
		alias:alias
	    }})
	.error(errorService.handleHttpError('Fasit sjekk om  eksisterer'))
	.success(function(data) {
	    console.log("finnes i fasit", data);
	    vm.validation.dmgrMissing=!data;
	});
    };
    
    

    this.estimatedPrice = function() {
	var unitCost = 600 + 732 + this.data.cpuCount * 100 + this.data.memory * 0.4;
	if(vm.settings.classification.type==='custom'){
	    unitCost=unitCost*2;
	}
	return this.data.serverCount * unitCost;
    }

    this.submitOrder = function() {
	this.data.classification=vm.settings.classification.type;
	this.data.description=vm.settings.classification.description;
	console.log("creating new jboss order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/was/node', this.data);
    };

} ];
