'use strict';

module.exports = [ '$http', "errorService", function($http, errorService) {

	var choices;

	return {
		restrict : 'E',
		scope : {
			model : '=',
			onSelect : '&',
			data : "=",
			queueManager : "=",
		},
		controller : [ "$scope", function($scope) {
			require('../../utils/util').initTooltips();

			var ctrl = this;
			this.updateChoices = function() {
				// console.log("updatechoices", ctrl.data);
				if (ctrl.data.environmentClass && ctrl.data.queueManager) {
					$http.get("rest/orders/mq/queue/clusters", {
						'params' : ctrl.data,
						cache : true
					}).then(function(response) {
						ctrl.choices = response.data;
					}, function errorCallback(response) {
						delete ctrl.choices;
						console.log("error getting clusters status", response.status, "data:", response.data)
					});
				}
			}
			this.sortBy= function(name){
				var guessName = guessClusterName();
				if(name === guessName ){
					return 1;
				}
				return 100;
				
			}
			
			this.isBestGuessClusterName = function(name){
				return name === guessClusterName();
			}
			
			function guessClusterName() {
				var envs = {
					"u" : "DEV",
					"t" : "TEST",
					"q" : "QASS",
					"p" : "PROD"
				}
				var envName = "";
				if (ctrl.data.environmentName) {
					envName = ctrl.data.environmentName.toUpperCase();
				}
				var name = "NL." + envs[ctrl.data.environmentClass] + "."+ envName + ".CLUSTER";
				
				return name;
			}

			$scope.$on("UpdateClustersEvent", function(event, e) {
//				console.log("event", e);
				ctrl.updateChoices()
			})

		} ],

		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-cluster.html"
	};
} ];
