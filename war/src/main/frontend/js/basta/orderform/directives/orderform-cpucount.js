'use strict';

module.exports = [ function () {

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                showValidation: "=",
                choices: "=",
                onSelect: '&onSelect'
            },

            controller:  function () {
            	
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-cpucount.html"
        };
    }];

