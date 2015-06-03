'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            scope: {
                model: '='
            },
            controllerAs: "ctrl",
            templateUrl: "basta/orderdetails/directives/orderdetails-operations.html"
        };
    };

