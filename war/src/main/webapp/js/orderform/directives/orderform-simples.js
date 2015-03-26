'use strict';

angular.module('basta.orderform-simples', [])
    .directive('orderformSimples', function () {
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
                    ? "partials/orderform/directives/orderform-" +attributes.customTemplate+ ".html"
                    : "partials/orderform/directives/orderform-simples.html";
                }
        };
    });

