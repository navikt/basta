'use strict';

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
		    //$location.url('order_list');
		    $scope.logout();
		}
	    });

	function setCurrentUser() {
	    User.current().then(function(userData) {
//		 console.log("current user", userData);
		$scope.currentUser = userData;
	    });
	}

	// Oppdatere innlogget bruker
	$scope.$on('UserChanged',   setCurrentUser);

	// sette bruker ved reload
	setCurrentUser();
	$scope.showLoginForm = false;
	this.userForm = {};

	$scope.showLogin = function() {
	    $scope.showLoginForm = true;
	    setTimeout(function() {
		$('#login_username').focus();
	    }, 200);
	};

	this.login = function() {
	    // console.log("logging in", this.userForm);
	    User.login(this.userForm.username, this.userForm.password);
	    delete this.userForm;
	    $scope.showLoginForm = false;
	};

	$scope.logout = function() {
//	    console.log("logging out");
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
	bindToController : true,
    };
} ];
