'use strict';

module.exports = [ 'User',  function (User) {
	  var isSuperUser = function (superuser) {
          this.superuser = superuser;
      };
	
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                label: '=',
                showValidation: "=",
                choices:"=",
                onSelect: '&onSelect'
            },

            controller: ['$scope', function ($scope) {
                $scope.$on('UserUpdated', function(){
                    User.sudo().then(isSuperUser.bind(this));
                }.bind(this));
                User.sudo().then(isSuperUser.bind(this));
               
                this.inSuperUserMode=false;
                this.toogleSuperuser = function(){
                	this.inSuperUserMode= !this.inSuperUserMode;
                }
            }],
            
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-dropdown.html"
        };
    }];

