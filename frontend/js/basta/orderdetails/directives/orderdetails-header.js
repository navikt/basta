'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            templateUrl: 'js/basta/orderdetails/directives/orderdetails-header.html',
            require: 'ngModel',
            controllerAs: "orderDetails"
        };
    };

