'use strict';

module.exports = function () {
    return {
        restrict: 'E',
        scope: {
            formvalid: '=',
            onSubmit: '&'
        },

        controller: function () {
            this.busy = false
            this.submit = function () {
                if (this.formvalid) {
                    this.busy = true
                    this.onSubmit()
                } else {
                    console.log('form is not valid. Check errors')
                }
            }

        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-buttons.html"
    };
}

