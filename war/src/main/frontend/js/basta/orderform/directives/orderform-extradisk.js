'use strict';

module.exports = [ function () {
	
		var choices= [1,5,10,20];

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                showValidation: "=",
                onSelect: '&onSelect'
            },

            controller:  function () {
            	this.choices=choices;
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-extradisk.html"
        };
    }];

