'use strict';

module.exports = ['User',  function (User) {
        var isSuperUser = function (superuser) {
            this.superuser = superuser;
        };

        return {
            restrict: 'E',
            scope: {
                formvalid: '=',
                onSubmit: '&',
            },

            controller: ['$scope', function ($scope) {
//        	var vm=this;
//        	
//        	User.onchange(function(){
//        	    vm.superuser=User.isSuperuser();
//        	});

                this.busy=false;
                
                this.submit = function(){
                    if(this.formvalid){
                    	this.busy=true;
                    	this.onSubmit();
                    }else {
                    	console.log('form is not valid. Check errors ')	
                    };
                }
               
            }],
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-buttons.html"
        };
    }];

