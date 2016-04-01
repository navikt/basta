'use strict';

module.exports = ["$timeout", function ($timeout) {

    return {
        restrict: 'E',
        scope: {
            internal: '=',
            model: '=',
            label: '@',
            placeholder: '@',
            maxLength: "@",
            minLength: '@',
            pattern: "@",
            showValidation: "=",
            required: '=',
            onChange: '&',
        },

        controller: function () {


            var vm = this;
            var valMessage = [];

            if (_.isEmpty(vm.internal)){
                valMessage.push("Angi minst en contextroot " + this.maxLength);
            }

            this.validationMessage = 'Valideringsregler: ' + valMessage.join(", ");

            this.updatemodel = function (a) {
                if (_.isEmpty(vm.internal)){
                    valMessage.push("Angi minst en contextroot " + this.maxLength);
                }
                $timeout(function () {
                    vm.model = _.flatten(_.map(vm.internal, _.values)).join();
                }, 10);
                vm.validationMessage = 'Valideringsregler: ' + valMessage.join(", ");

            }

        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-multivalue.html"
    };
}];

