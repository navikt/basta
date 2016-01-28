'use strict';

module.exports = [ function() {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			label : '@',
			showValidation : "=",
			inEditMode : '=',
			maxLength:"@"
		},

		controller : function() {
			var vm = this;

			this.toggleEditMode = function() {
				vm.inEditMode = !vm.inEditMode;
			}
		},

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-mq-name.html"
	};
} ];
