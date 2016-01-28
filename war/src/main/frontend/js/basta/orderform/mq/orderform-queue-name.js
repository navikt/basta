'use strict';

module.exports = [ function() {

	return {
		restrict : 'E',
		scope : {
			name : '=',
			label : '=',
			generatedlabel : '=',
			showValidation : "=",
			generated : '=',
			generator : '&',
		},

		controller : function() {
			var vm = this;
			this.inEditMode = false;

			this.generateName = function() {
				if (!vm.inEditMode) {
					vm.generated = vm.generator(vm.name);
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
