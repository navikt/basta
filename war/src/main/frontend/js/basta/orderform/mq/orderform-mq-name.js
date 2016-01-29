'use strict';

module.exports = [ function() {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			label : '@',
			showValidation : "=",
			inEditMode : '=',
			maxLength: "@",
			minLength: '@',
			pattern : "@"
		},

		controller : function() {
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
			
			this.toggleEditMode = function() {
				vm.inEditMode = !vm.inEditMode;
			}
		},

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-mq-name.html"
	};
} ];
