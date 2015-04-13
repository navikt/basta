'use strict';

module.exports = function () {
    require("angular-bootstrap-checkbox");
        return {
            restrict: 'E',
            scope: {
                showValidation: "=",
                data: '=model',
                onSelect: '&onSelect',
                choices : '=' ,
                labelText: '=',
                labelDescription: '=',
                image: '=',
                customTemplate : '=',
                config: '='
            },

            controller: function () {
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl:  function(element, attributes) {
                return attributes.customTemplate
                    ? "js/basta/orderform/directives/orderform-" +attributes.customTemplate+ ".html"
                    : "js/basta/orderform/directives/orderform-simples.html";
                }
        };
    };

