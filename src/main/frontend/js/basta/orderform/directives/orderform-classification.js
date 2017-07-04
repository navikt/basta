'use strict';

module.exports = [ 'User', function(User) {

    return {
	restrict : 'E',
	scope : {
	    data : '=model',
	    showValidation : "=",
	    onSelect : '&onSelect',
	    

	},
	controller : function() {
	    


	},
	controllerAs : 'ctrl',
	bindToController : true,
	templateUrl : "basta/orderform/directives/orderform-classification.html"
    };
} ];
