'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", "$http", "errorService", function($scope, User, BastaService, $http, errorService) {

    this.choices = {
	memory : [ 2, 3, 4, 6, 8, 10, 12, 14, 16 ],
	serverCount : [ 1, 2, 4, 6, 8, 10 ]
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
	nodeType : 'WAS_NODES',
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
	this.validation.fasitPrerequisite = false;
    }

    this.changeZone = function() {
	checkFasit();
    }

    this.changeEnvironment = function() {
	checkFasit();
    }

    function checkFasit() {
	$http.get('rest/vm/orders/was/node/validation', {
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
	var unitCost = 600 + 732 + this.data.cpuCount * 100 + this.data.memory * 0.4;
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
