'use strict';

module.exports = [ 'User', function(User) {

    function Controller() {
	var vm = this;

	this.data = 'u';

	User.onchange(function() {
	    var user = User.current();
	    vm.envClasses = enrichWithUserAccess(user);
	});

	function enrichWithUserAccess(userData) {
	    var classes = userData.environmentClasses;
	    var classesWithNames = [ {
		key : 'u',
		name : 'Utvikling'
	    }, {
		key : 't',
		name : 'Test'
	    }, {
		key : 'q',
		name : 'PreProd'
	    }, {
		key : 'p',
		name : 'Produksjon'
	    } ];

	    return _.chain(classesWithNames).map(function(envClass) {
		envClass.hasAccess = classes.indexOf(envClass.key) > -1;
		return envClass;
	    }).value();
	}

    }

    return {
	restrict : 'E',
	scope : {
	    data : '=model',
	    onSelect : '&onSelect'
	},
	controller : Controller,
	controllerAs : 'ctrl',
	bindToController : true,
	templateUrl : "basta/orderform/directives/orderform-environmentclasses.html"
    };
} ];
