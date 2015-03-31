'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            templateUrl: 'partials/orderdetails/orderdetails-header.html',
            require: 'ngModel',
            controllerAs: "orderDetails"
        };
    };

