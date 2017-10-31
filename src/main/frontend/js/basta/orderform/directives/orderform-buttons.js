'use strict';

module.exports = [ "$timeout", function($timeout) {
    return {
        restrict: 'E',
        scope: {
            formvalid: '=',
            busy: '=?',
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
}]

