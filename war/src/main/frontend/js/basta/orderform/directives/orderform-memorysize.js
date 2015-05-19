'use strict';

module.exports = [ function () {
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                showValidation: "=",
                choices:"=",
                onSelect: '&onSelect'
            },

            controller: function () {
            	//console.log(choices)
//            	 this.choices=[512,1024,2048,4096];	
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-memorysize.html"
        };
    }];

