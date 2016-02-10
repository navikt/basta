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
		controller : [ "$scope", function($scope) {
			require('../../utils/util').initTooltips();
			var ctrl = this;
			
			FasitService.queueManagers().then(function(data) {
				ctrl.choices = data;
			});
			
			function setBestGuess(){
				var bestGuess = ctrl.choices[ctrl.envClassKey].filter(function(qm){
					return ctrl.isUsedByApplication(qm);
				});
				if (bestGuess.length >0){
					ctrl.model=bestGuess[0].url;
				}
				
			}
			
			this.orderByUsed= function(qm){
				if(ctrl.isUsedByApplication(qm)){
					return 1;
				}
				return 100;
			}

			this.isUsedByApplication = function(qm) {
				return qm && qm.usedby.indexOf(ctrl.application) != -1;
			}
			

			$scope.$on("UpdateQueueManangerEvent", function(event, e) {
//				console.log("event", e);
				setBestGuess();
			})

		}],
		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-managers.html"
	};
} ];
