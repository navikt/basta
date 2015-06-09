'use strict';

module.exports = [ 'User', function(User) {
  
    function AuthController() {
	var vm= this;
	User.subscribe(function(){
	    vm.authenticated=User.currentUser().authenticated;
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
