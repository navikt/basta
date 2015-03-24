'use strict';

angular.module('basta.orderform-validation', [])
    .directive('orderformValidation', function () {
        return {
            restrict: 'E',
            scope:{
                validationText: '='
            },
            controller: function(){
                console.log(this);
                this.valtext="Test";
            },
            controllerAs: 'valCtrl',
            bindToController: true,
            templateUrl: function(e, attributes) {return "partials/orderform/validation/validation-" +attributes.type+ ".html"}
        };
    });

