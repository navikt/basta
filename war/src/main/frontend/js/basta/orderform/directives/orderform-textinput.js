'use strict';

module.exports = [function () {
        
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
            	if(! this.minLength){
            		console.log("MinLength is not set. Using default 5")
            		this.minLength=5;
            	}
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-textinput.html"
        };
    }];

