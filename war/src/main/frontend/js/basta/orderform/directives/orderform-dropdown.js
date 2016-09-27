'use strict';

module.exports = ['User', function (User) {

    return {
        restrict: 'E',
        scope: {
            data: '=model',
            label: '=',
            showValidation: "=",
            choices: "=",
            choiceSuffix: "=?",
            optional: "=?",
            noOverride: "@",
            onSelect: '&',
            type: "@",
            disabled: "="
        },

        controller: function () {
            var that = this;

            // set default to false
            if (!that.noOverride) {
                that.noOverride = false;
            }

            User.onchange(function () {
                that.superuser = User.isSuperuser() && !that.noOverride;
            });

            this.inSuperUserMode = false;
            this.toogleSuperuser = function () {
                this.inSuperUserMode = !this.inSuperUserMode;
            }
        },

        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: function (e, attributes) {
            if (attributes.type === "object") {
                return "basta/orderform/directives/orderform-dropdown-object.html"
            } else {
                return "basta/orderform/directives/orderform-dropdown.html"
            }
        }
    };
}];

