'use strict';

module.exports = [ function() {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			label : '@',
			showValidation : "=",
			inEditMode : '=',
			maxLength:"@",
			pattern : "@"
		},

		controller : function() {
			var vm = this;
        	if(! this.minLength){
        		this.minLength=5;
        	}

			if (this.pattern){
				this.validationMessage='Verdien må matche regexp ' +this.pattern +'  og ha en maks lengde på ' + vm.maxLength;
			}else{
				this.validationMessage='Verdien må ha en maks lengde på ' + vm.maxLength;
			}
						
			
			
			this.toggleEditMode = function() {
				vm.inEditMode = !vm.inEditMode;
			}
		},

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-mq-name.html"
	};
} ];
