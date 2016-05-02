'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", "$http", "errorService",'$routeParams', function($scope, User, BastaService, $http, errorService, $routeParams) {

    this.validation={
	fasitPrerequisite:false,
	fasitDetails: []
	
    };
    
    this.settings={
	    classification: {type: 'standard'}
    }

    this.data = {
	nodeType : 'WAS_DEPLOYMENT_MANAGER',
	environmentClass: $routeParams.environmentClass || 'u',
	zone: $routeParams.zone || 'fss',
	environmentName: $routeParams.environmentName || null,
	cpuCount: 2,
	memory: 4,
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
	$http.get('rest/vm/orders/was/dmgr/validation', {
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
    
    this.submitOrder = function() {
	this.data.classification=vm.settings.classification.type;
	this.data.description=vm.settings.classification.description;
	console.log("creating new was dmgr order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/was/dmgr', this.data);
    };

} ];
