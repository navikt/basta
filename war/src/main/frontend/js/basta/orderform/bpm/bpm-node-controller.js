'use strict';

var angular = require('angular');

module.exports = [ "BastaService", "$http", "errorService", function(BastaService, $http, errorService) {

    this.choices = {
	memory : [ 8, 16 ],
	serverCount : [ 1, 2, 3, 4, 5, 6, 8 ],
	cpuCount : [ 2, 4 ]
    }

    this.validation = {
	fasitPrerequisite : false,
	fasitDetails : []

    };

    this.data = {
	nodeType : 'BPM_NODES',
	environmentClass : 'u',
	zone : 'fss',
	applicationMappingName : null,
	environmentName : null,
	cpuCount : 4,
	serverCount : 1,
	memory : 16,
	extraDisk : 10,
    }

    var vm = this;

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	this.validation.fasitPrerequisite = false;
    }

    this.changeEnvironment = function() {
	checkFasit();
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
