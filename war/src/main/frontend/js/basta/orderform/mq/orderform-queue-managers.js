'use strict';

module.exports = [ 'FasitService', function(FasitService) {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			showValidation : "=",
			onSelect : '&',
			envClassKey : '=',
			application : '='
		},
		controller : function() {
			require('../../utils/util').initTooltips();
			var ctrl = this;
			
			FasitService.queueManagers().then(function(data) {
				ctrl.choices = data;
			});

			this.isUsedByApplication = function(qm) {
				return qm && qm.usedby.indexOf(ctrl.application) != -1;
			}

		},
		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-managers.html"
	};
} ];
