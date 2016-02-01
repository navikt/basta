'use strict';

module.exports = [ function() {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			label : '@',
			showValidation : "=",
			inEditMode : '=?',
			maxLength: "@",
			minLength: '@',
			pattern : "@",
			tooltip :"@"	
		},

		controller : function() {
			require('../../utils/util').initTooltips();;
			
			var vm = this;
			var valMessage=[];
			if (this.pattern){
				valMessage.push("matche regexp " + this.pattern ); 
			}
			if(this.minLength){
				valMessage.push("minimum lengde "+ this.minLength ); 
			}
			if(this.maxLength){
				valMessage.push("maximum lengde "+ this.maxLength ); 
			}
			this.validationMessage= 'Valideringsregler: '+ valMessage.join(", ");
			
			if(angular.isUndefined(vm.inEditMode)){
				vm.inEditMode=false;
			}
			
			this.toggleEditMode = function() {
				vm.inEditMode = !vm.inEditMode;
			}
		},

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/directives/orderform-generated-text.html"
	};
} ];
