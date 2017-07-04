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
				if (ctrl.data.environmentClass && ctrl.data.queueManager) {
					$http.get("rest/v1/mq/clusters", {
						'params' : ctrl.data,
						cache : true
					}).then(function(response) {
						console.log("clusters", response.data);
						ctrl.choices = response.data;
					}, function errorCallback(response) {
						console.log("error getting clusters status", response.status, "data:", response.data);
						delete ctrl.choices;
					});
				}
			};

			this.isRequired = function () {
				if(ctrl.checked){
					ctrl.model=guessClusterName();
				}else{
					delete ctrl.model;
				}
			};
			
			this.hasClusterNameInChoices= function(){
				var hasChoiceVar = hasChoice();
				if(!hasChoiceVar){
					ctrl.checked=false;
					delete ctrl.model;
				}
				return hasChoiceVar;
			};
			
			this.showWarning= function (){
				if(ctrl.data.environmentName && ctrl.data.queueManager){
					return !hasChoice();
				}
				return false;
			};
			
			function hasChoice() {
				return ctrl.choices && ctrl.choices.indexOf(guessClusterName())!=-1;
			}
			
			function guessClusterName() {
				if (ctrl.data.environmentClass === 'u') {
					return "NL.DEV.D1.CLUSTER";
				}
				var envs = {
					"u" : "DEV",
					"t" : "TEST",
					"q" : "QASS",
					"p" : "PROD"
				};
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
