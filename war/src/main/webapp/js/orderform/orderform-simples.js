'use strict';

angular.module('basta.orderform-simples', [])
    .directive('orderformSimples', function () {
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                onSelect: '&onSelect',
                labelText: '=',
                choices : '=',
            },

            controller: function () {
                console.log(this);
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-simples.html"
        };
    });

