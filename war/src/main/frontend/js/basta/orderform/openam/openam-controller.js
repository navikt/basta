'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", function($scope, User, BastaService) {

    this.choices = {
	serverCount : [ 1, 2 ]
    }

    this.data = {
	nodeType : 'OPENAM12_SERVER',
	environmentClass : 'u',
	zone : 'sbs',
	environmentName : null,
	cpuCount : 2,
	serverCount : 1,
	memory : 2
    }

    var vm = this;

    User.onchange(function() {
	vm.superuser = User.isSuperuser();
    });

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
	delete vm.validation.fasitPrerequisite;
    }
    
    this.changeEnvironment = function() {
	validate();
    }

    function validate() {
	$http.get('rest/vm/orders/openam/validation', {
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
	BastaService.submitOrderWithUrl('rest/vm/orders/openam', this.data);
    };

} ];
