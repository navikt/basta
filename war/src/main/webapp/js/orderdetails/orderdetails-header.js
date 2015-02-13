'use strict';

angular.module('basta.orderdetails-header', [])
    .directive('orderdetailsHeader', function () {
        return {
            restrict: 'E',
            templateUrl: 'partials/orderdetails/orderdetails-header.html',
            require: 'ngModel',
            controllerAs: "orderDetails"
        };
    });

