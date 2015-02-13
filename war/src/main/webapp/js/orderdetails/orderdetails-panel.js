'use strict';

angular.module('basta.orderdetails-panel', [])
    .directive('orderdetailsPanel', function () {
        return {
            restrict: 'E',
            templateUrl: function(element, attributes) {return "partials/orderdetails/" +attributes.panel+ ".html"},
            require: 'ngModel',
            controllerAs: "orderDetails"
        };
    });

