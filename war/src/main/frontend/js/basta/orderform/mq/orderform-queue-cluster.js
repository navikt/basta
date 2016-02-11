'use strict';

module.exports = [ '$http', "errorService", function($http, errorService) {

	var choices;
	var checked;

	return {
		restrict : 'E',
		scope : {
			model : '=',
			onSelect : '&',
			data : "=",
		},
		controller : [ "$scope", function($scope) {
			require('../../utils/util').initTooltips();

			var ctrl = this;
			this.updateChoices = function() {
//				console.log("updatechoices", ctrl.data);
				if (ctrl.data.environmentClass && ctrl.data.queueManager) {
					$http.get("rest/orders/mq/queue/clusters", {
						'params' : ctrl.data,
						cache : true
					}).then(function(response) {
						ctrl.choices = response.data;
					}, function errorCallback(response) {
						console.log("error getting clusters status", response.status, "data:", response.data)
						delete ctrl.choices;
					});
				}
			}
			
			this.onChange= function(){
				if(ctrl.checked){
					ctrl.model=guessClusterName();
				}else{
					delete ctrl.model;
				}
			}
			
			this.hasClusterNameInChoices= function(){
				var hasChoiceVar = hasChoice();
				if(!hasChoiceVar){
					ctrl.checked=false;
					delete ctrl.model;
				}
				return hasChoiceVar;
			}
			
			this.showWarning= function (){
				if(ctrl.data.environmentName && ctrl.data.queueManager){
					return !hasChoice();
				}
				return false;
			}
			
			function hasChoice() {
				return !angular.isUndefined(ctrl.choices) && ctrl.choices.indexOf(guessClusterName())!=-1;
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
