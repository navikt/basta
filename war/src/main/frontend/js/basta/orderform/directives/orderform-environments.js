'use strict';

module.exports =['FasitService', function (FasitService) {

        return {
            restrict: 'E',
            scope: {
                data :'=model',
                showValidation: "=",
                onSelect :'&',
                envClassKey:'=',
                customMultisiteAlert:'='
            },
            controller: function () {
        	var vm= this;
                FasitService.environments.then( function(data){
                    vm.choices=data;
                });
        	
        	this.isMultiSite = function(name){
        	    if(vm.envClassKey==='p'){
        		    return true;
        		}
        	        return  _(['q0','q1','q3']).contains(name);
        	}

            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-environments.html"
        };
    }];

