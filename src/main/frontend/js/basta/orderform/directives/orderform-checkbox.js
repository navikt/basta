'use strict';

module.exports = [function() {

    return {
	restrict : 'E',
	scope : {
	    model : '=',
		isRequired: "&", 
	    label:'@',
	    tooltip :'@'
	    

	},
	controller : function() {
		require('../../utils/util').initTooltips();


	},
	controllerAs : 'ctrl',
	bindToController : true,
	templateUrl : "basta/orderform/directives/orderform-checkbox.html"
    };
}] ;
