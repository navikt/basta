'use strict';
var util = require('../utils/util');

module.exports = [ 'User', 'hotkeys', function(User, hotkeys) {
    function AuthController($scope) {
	hotkeys.add({
	    combo : 'i',
	    description : 'Log in',
	    callback : function() {
		$scope.showLogin();
	    }
	});

	hotkeys.add({
	    combo : 'o',
	    description : 'Logout',
	    callback : function() {
		// $location.url('order_list');
		$scope.logout();
	    }

	});
    util.initTooltips();
    var vm= this;


	User.onchange(function(){
	  vm.user=User.current();  
	}); 
	
	$scope.showLoginForm = false;
	this.userForm = {};

	$scope.showLogin = function() {
	    $scope.showLoginForm = true;
	    setTimeout(function() {
		$('#login_username').focus();
		}, 50);
	};

	this.login = function() {
	    // console.log("logging in", this.userForm);
	    User.login(this.userForm.username, this.userForm.password);
	    delete this.userForm;
	    $scope.showLoginForm = false;
	};

	$scope.logout = function() {
	    // console.log("logging out");
	    User.logout();
	};

    }
    // Triks for å unngå problemer med inject av scope i directiv
    AuthController.$inject = [ "$scope" ];

    return {
	restrict : 'E',
	templateUrl : 'basta/directives/login.html',
	scope : {

	},
	controller : AuthController,
	controllerAs : 'ctrl',
	bindToController : true
    };
} ];
