'use strict';

module.exports = function () {
    return {
        restrict: 'E',
        scope: {
            formvalid: '=',
            onStart: '&',
            onStop: '&',
            onDelete: '&',
            disabled: '=',
        },

        controller: function () {
            this.busy = false;
            this.submit = function (type) {
                console.log(this.formvalid);
            	
                if (this.formvalid) {
                    this.busy = true;
                    switch (type) {
					case 'START':
                        this.onStart();
						break;
					case 'STOP':
                        this.onStop();
						break;
					case 'DELETE':
                        this.onDelete();
						break;
					}
                } else {
                    console.log('form is not valid. Check errors')
                }
            }
            
        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/order-operation/directives/operation-buttons.html"
    };
};

