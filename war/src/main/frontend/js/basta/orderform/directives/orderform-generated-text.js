'use strict';

module.exports = [ "$timeout", function($timeout) {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			label : '@',
			showValidation : "=",
			inEditMode : '=?',
			maxLength : "@",
			minLength : '@',
			pattern : "@",
			tooltip : "@",
			isRequired: '&',
		},

		controller : function() {
			require('../../utils/util').initTooltips();

			var vm = this;
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

			if (angular.isUndefined(vm.inEditMode)) {
				vm.inEditMode = false;
			}

			this.toggleEditMode = function() {
				vm.inEditMode = !vm.inEditMode;
			};

			this.change = function() {
				// reagerer kun på events når data er gyldige. Modellen settes til undefined ved ugyldige data
				if (vm.model) {
					// Triks for å få unngå problemer med at modellen oppdateres etter event har kjørt
					$timeout(function() {
						vm.isRequired();
					}, 10);
				}

			}
		},

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/directives/orderform-generated-text.html"
	};
} ];
