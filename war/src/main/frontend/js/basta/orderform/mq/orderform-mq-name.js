'use strict';

module.exports = [ function() {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			label : '@',
			showValidation : "=",
			inEditMode : '=',
		},

		controller : function() {
			var vm = this;

			this.generateName = function() {
				if (!vm.inEditMode) {
					vm.model = vm.generator();
				}
			}

			this.toggleEditMode = function() {
				vm.inEditMode = !vm.inEditMode;
			}
		},

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-name.html"
	};
} ];
