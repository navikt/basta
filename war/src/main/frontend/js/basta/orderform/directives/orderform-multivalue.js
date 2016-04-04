'use strict';

module.exports = ["$timeout", function ($timeout) {

    return {
        restrict: 'E',
        scope: {
            internal: '=',
            model: '=',
            invalidValues: '=',
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

            console.log(vm.externalValidation);


            this.validate = function () {
                valMessage = [];
                if (_.isEmpty(vm.internal)) {
                    valMessage.push("Angi minst en contextroot.");
                }
                if (vm.invalidValues) {
                    var x = _.intersection(_.map(vm.internal, _.values), vm.invalidValues)
                    console.log(_.map(vm.internal, _.values) + ' vs ' + vm.invalidValues);
                    if (x) {
                        valMessage.push('Ugyldige verdier: ' + x);
                    }
                }
            };

            this.validate();

            this.validationMessage = 'Valideringsregler: ' + valMessage.join(", ");

            this.updatemodel = function (a) {
                this.validate();
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

