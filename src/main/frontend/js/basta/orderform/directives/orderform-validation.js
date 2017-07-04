'use strict';

module.exports = function () {
        return {
            restrict: 'E',
            scope:{
                validationText: '='
            },
            controller: function(){},
            controllerAs: 'valCtrl',
            bindToController: true,
            templateUrl: function(e, attributes) {return "basta/orderform/directives/orderform-validation-" +attributes.type+ ".html"}
        };
    };

