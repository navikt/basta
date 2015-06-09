'use strict';

module.exports = [ 'User',  function (User) {
	
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                label: '=',
                showValidation: "=",
                choices:"=",
                onSelect: '&onSelect'
            },

            controller:  function () {
        	var vm=this;
        	
        	User.onchange(function(){
        	    vm.superuser=User.isSuperuser();
        	});
               
                this.inSuperUserMode=false;
                this.toogleSuperuser = function(){
                	this.inSuperUserMode= !this.inSuperUserMode;
                }
            },
            
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-dropdown.html"
        };
    }];

