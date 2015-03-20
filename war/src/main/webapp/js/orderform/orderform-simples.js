'use strict';

angular.module('basta.orderform-simples', [])
    .directive('orderformSimples', function () {
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                onSelect: '&onSelect',
                choices : '=' ,
                labelText: '=',
                labelDescription: '=',
                image: '=',
                customTemplate : '='
            },

            controller: function () {  },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl:  function(element, attributes) {
                console.log(attributes.customTemplate);
                return attributes.customTemplate
                    ? "partials/orderform/orderform-" +attributes.customTemplate+ ".html"
                    : "partials/orderform/orderform-simples.html";
                }
        };
    });

