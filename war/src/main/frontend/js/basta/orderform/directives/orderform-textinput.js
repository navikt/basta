'use strict';

module.exports = [function () {
        
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                label: '=',
                showValidation: "=",
                required: '='
            },

            controller: function () {
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-textinput.html"
        };
    }];

