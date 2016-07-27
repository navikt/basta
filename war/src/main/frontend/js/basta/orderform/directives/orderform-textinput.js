'use strict';

module.exports = ['$timeout', 'User', function ($timeout, User) {

    return {
        restrict: 'E',
        scope: {
            data: '=model',
            label: '@',
            maxLength: "@",
            minLength: '@',
            pattern: "@",
            showValidation: "=",
            required: '=',
            onChange: '&',
            onlySuperuser: '@',
            tooltip: "@",
            onBlur: '&'
        },

        controller: function () {
            require('../../utils/util').initTooltips();
            var vm = this;

            if (this.onlySuperuser && !User.isSuperuser()) {
                this.readonly = true
            }

            var valMessage = [];

            if (this.pattern) {
                valMessage.push("matche regexp " + this.pattern);
            }
            if (this.minLength) {
                valMessage.push("minimum lengde " + this.minLength);
            }
            if (this.maxLength) {
                valMessage.push("maximum lengde " + this.maxLength);
            }

            this.validationMessage = 'Valideringsregler: ' + valMessage.join(", ");

            this.blur = function () {
                vm.onBlur()
            };

            this.change = function () {
                // Brukes for å få unngå problemer med at modellen oppdateres etter event har kjørt
                $timeout(function () {
                    vm.onChange();
                }, 10);
            }
        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-textinput.html"
    };
}];

