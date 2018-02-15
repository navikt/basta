'use strict';

var angular = require('angular');

module.exports = [ '$http',  "BastaService", 'errorService', '$routeParams',  function($http,  BastaService, errorService, $routeParams) {

    this.choices = {
	serverCount : [ 1, 2 ]
    }
    
    this.validation={};

    this.data = {
    		environmentClass: $routeParams.environmentClass || 'u',
    		environmentName: $routeParams.environmentName || null,
    		serverCount: $routeParams.serverCount || 1,
    		zone : 'sbs',
    }

    var vm = this;

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	delete vm.validation.fasitPrerequisite;
    }
    
    this.changeEnvironment = function() {
	validate();
    }

    function validate() {
	$http.get('rest/vm/orders/openam/server/validation', {
	    params : {
		environmentClass : vm.data.environmentClass,
		environmentName : vm.data.environmentName
	    }
	}).error(errorService.handleHttpError('Fasit sjekk om påkrevde ressurser eksisterer')).success(function(data) {
	    vm.validation.fasitPrerequisite = !_.isEmpty(data);
	    vm.validation.fasitDetails = data;
	});
    } 

    this.submitOrder = function() {
	console.log("creating new openam order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/openam/server', this.data);
    };

} ];
