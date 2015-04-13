'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            templateUrl: function(element, attributes) {return "js/basta/orderdetails/directives/" +attributes.panel+ ".html"},
            require: 'ngModel',
            controllerAs: "orderDetails"
        };
    };

