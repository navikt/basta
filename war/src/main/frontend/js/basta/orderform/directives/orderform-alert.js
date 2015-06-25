'use strict';

module.exports = [ function() {

    return {
	restrict : 'E',
	scope : {
	    label : '=',
	    showAlert : "=",
	    showValidation : "="
	},

	controller : function() {
	},
	controllerAs : 'ctrl',
	bindToController : true,
	templateUrl : "basta/orderform/directives/orderform-alert.html"
    };
} ];
