'use strict';

module.exports = [ 'User', function(User) {
  
    function AuthController() {
	var vm= this;
	User.onchange(function(){
	    vm.authenticated=User.isAuthenticated();
	});

    }

    return {
	restrict : 'E',
	scope : {
	    header : '=',
	    description : '=',
	    image : '='

	},
	controller : AuthController,
	controllerAs : 'authCtrl',
	bindToController : true,
	transclude : true,
	templateUrl : "basta/orderform/directives/orderform.html"

    };
} ];
