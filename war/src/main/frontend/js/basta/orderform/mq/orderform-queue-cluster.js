'use strict';

module.exports = [ '$http', "errorService",  function( $http, errorService) {
	
	var choices;

	return {
		restrict : 'E',
		scope : {
			model : '=',
			onSelect : '&',
			data : "=",
			queueManager : "=",
		},
		controller : ["$scope",function($scope) {
			require('../../utils/util').initTooltips();
			
			var ctrl = this;
			this.updateChoices= function(){
				console.log("updatechoices", ctrl.data);
				if(ctrl.data.environmentClass &&  ctrl.data.queueManager){
					$http.get("rest/orders/mq/queue/clusters", {'params':ctrl.data, cache:true}).then(function(response) {
						ctrl.choices=response.data;
					}, errorService.handleHttpError('Cluster lookup i MQ'));
				}
			}
			
			$scope.$on("QueueManagerEvent", function(event, e){
				console.log("event", e);
				ctrl.updateChoices()
			})

		}],

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-cluster.html"
	};
} ];
