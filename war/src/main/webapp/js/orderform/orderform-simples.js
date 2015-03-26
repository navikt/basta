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
                    ? "partials/orderform/orderform-" +attributes.customTemplate+ ".html"
                    : "partials/orderform/orderform-simples.html";
                }
        };
    });

