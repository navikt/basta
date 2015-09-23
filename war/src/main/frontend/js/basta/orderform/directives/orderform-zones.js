'use strict';

module.exports = [ 'User', function(User) {
    var zones = [ {
	key : 'fss',
	name : 'Fagsystemsone'
    }, {
	key : 'sbs',
	name : 'Selvbetjeningsone'
    } ];

    return {
	restrict : 'E',
	scope : {
	    data : '=model',
	    showValidation : "=",
	    onSelect : '&onSelect',
	    disable : "="

	},
	controller : function() {
	    var vm =this;
	    this.zones = filterDisabled(zones);
	    
	    function filterDisabled(zones) {
		var disable_zones = vm.disable || "";
		return _.chain(zones).map(function(zone) {
		    zone.isDisabled = disable_zones.indexOf(zone.key) > -1;
		    return zone;
		}).value();
	    }

	},
	controllerAs : 'ctrl',
	bindToController : true,
	templateUrl : "basta/orderform/directives/orderform-zones.html"
    };
} ];
