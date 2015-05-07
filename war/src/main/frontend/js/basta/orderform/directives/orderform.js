'use strict';

module.exports = ['User', function ( User) {
	 var setAuthenticated = function (auth) {
		  console.log("updated authentiation", auth);
         this.authenticated = auth;
     };
     // Triks for å unngå problemer med inject av scope i directiv 
     function AuthController($scope) {
    	 $scope.$on('UserUpdated', function(){
             User.authenticated().then(setAuthenticated.bind(this));
         }.bind(this));

         User.authenticated().then(setAuthenticated.bind(this));
    	}
     AuthController.$inject = ["$scope"];

        return {
            restrict: 'E',
            scope: {  
            	header: '=',
            	description: '=',
            	image: '='
            	
            },
            controller: AuthController,
            controllerAs: 'authCtrl',
            bindToController: true,
            transclude: true,
            templateUrl: "basta/orderform/directives/orderform.html"
            	
        };
    }];

