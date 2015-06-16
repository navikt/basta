'use strict';

module.exports = [ function() {

    return {
	restrict : 'E',
	scope : {
	    label : '=',
	    showAlert : "=",
	    type : "@?",
	    showValidation : "="
	},

	controller : function() {
	},
	controllerAs : 'ctrl',
	bindToController : true,
	templateUrl : function(elem, attr) {
	    console.log("type" , attr.type)
	    if (attr.type === 'error') {
		return "basta/orderform/directives/orderform-alert-error.html";
	    }
	    return "basta/orderform/directives/orderform-alert.html";
	}
    };
} ];
