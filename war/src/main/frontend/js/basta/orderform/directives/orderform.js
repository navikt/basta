'use strict';

module.exports = ['User', function ( User) {
	 var setAuthenticated = function (auth) {
		  console.log("updated", this, auth);
         this.authenticated = auth;
     };
     // Triks for å unngå problemer med inject av scope i directiv 
     function AuthController($scope) {
    	 $scope.$on('UserUpdated', function(){
             User.authenticated().then(setAuthenticated.bind(this));
         }.bind(this));

         User.authenticated().then(setAuthenticated.bind(this));
         console.log('controller');
    	}
     AuthController.$inject = ["$scope"];

        return {
            restrict: 'E',
            scope: {
            	
            },
            controller: AuthController,
            controllerAs: 'ctrl',
            bindToController: true,
            transclude: true,
            templateUrl: "basta/orderform/directives/orderform.html"
            	
        };
    }];

