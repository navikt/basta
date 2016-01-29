'use strict';

module.exports = ["$timeout", function ($timeout) {
        
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                label: '@',
                minLength: '@',
                showValidation: "=",
                required: '=',
                onChange: '&',
            },

            controller: function () {
            	var vm=this;
            	if(! this.minLength){
//            		console.log("MinLength is not set. Using default 5")
            		this.minLength=5;
            	}
            	
            	this.change= function(){
            		// Brukes for å få unngå problemer med at modellen oppdateres etter event har kjørt
            		$timeout(function(){
            			  vm.onChange();
            		},10);

            	}
            	
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-textinput.html"
        };
    }];

