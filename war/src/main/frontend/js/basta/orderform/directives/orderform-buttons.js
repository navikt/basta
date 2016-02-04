'use strict';

module.exports = [ "$timeout", function($timeout) {
    return {
        restrict: 'E',
        scope: {
            formvalid: '=',
            onSubmit: '&'
        },

        controller: function () {
        	var vm=this;
            this.busy = false
            this.submit = function () {
                if (this.formvalid) {
                    this.busy = true
                    this.onSubmit()
                    // Venter litt før vi tar bort busy igjen
                    $timeout(function() {
						vm.busy=false;
					}, 10000);
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

