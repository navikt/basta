'use strict';

angular.module('basta.orderform-validation', [])
    .directive('orderformValidation', function () {
        return {
            restrict: 'E',
            scope:{
                validationText: '='
            },
            controller: function(){},
            controllerAs: 'valCtrl',
            bindToController: true,
            templateUrl: function(e, attributes) {return "partials/orderform/directives/validation/validation-" +attributes.type+ ".html"}
        };
    });

