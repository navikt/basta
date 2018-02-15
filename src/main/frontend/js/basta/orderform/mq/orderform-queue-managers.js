'use strict';

module.exports = [ 'queueManagerService', function( queueManagerService) {

	return {
		restrict : 'E',
		scope : {
			model : '=',
			showValidation : "=",
			onSelect : '&',
			envClassKey : '=',
			environmentName : '=',
			application : '='
		},
		controller : [ "$scope", function($scope) {
			require('../../utils/util').initTooltips();
			var ctrl = this;
			
			queueManagerService.createAliasApplicationMap().then(function(data) {
				console.log("alias application mapping", data);
				ctrl.aliasApplicationMap = data;
				updateChoices();
			});
			
			function updateChoices(){
//				console.log("updating", ctrl.environmentName, ctrl.application);
				if (ctrl.envClassKey && ctrl.environmentName && ctrl.application) {
					queueManagerService.getQueueManagers(ctrl.envClassKey, ctrl.environmentName, ctrl.application).then(function(data) {
						console.log("queuemanagers", ctrl.environmentName, data);
						ctrl.choices = data;
						setBestGuess();
					});
				} else {
					delete ctrl.choices;
				}
			}

			$scope.$on("UpdateQueueManangerEvent", function(event, e) {
//				console.log("event", ctrl);
				updateChoices();
			})


			function setBestGuess() {
				var bestGuess = ctrl.choices.filter(function(qm) {
					return ctrl.isUsedByApplication(qm);
				});
//				console.log("bestguess", bestGuess, ctrl.choices);
				if (bestGuess.length > 0) {
					// picking the one that is used before
					ctrl.model = bestGuess[0].url;
				} else {
					// picking a random one from the standard
					// ctrl.model =
					// _.chain(choicesForEnv).shuffle().first().value().url;
					delete ctrl.model;
				}

			}

			this.orderByUsed = function(qm) {
				if (ctrl.isUsedByApplication(qm)) {
					return 1;
				}
				return 100;
			}

			this.getAliasUsedByApplication= function() {
				if (!ctrl.aliasApplicationMap) {
					return [];
				}
				return _.chain(ctrl.aliasApplicationMap).filter(function(item) {
					return _.contains(item.usedby, ctrl.application);
				}).pluck("alias").value();
			}

			function getSelectedQm() {
				if (ctrl.choices && ctrl.model) {
					return _.find(ctrl.choices, function(choice) {
						return choice.url === ctrl.model;
					})
				}
			}

			this.isUsedByApplication = function(qm) {
				if (!qm) {
					// fallback til å sjekke valgt qm om den ikke er oppgitt
					qm = getSelectedQm();
				}
				if (!qm) {
					return false;
				}
				return _.intersection(ctrl.getAliasUsedByApplication(), qm.alias).length > 0;
			}

			this.isUsedByOtherQueueMananger = function() {
				var selected = getSelectedQm();
				if (ctrl.isUsedByApplication(selected)) {
					return false;
				}
				return ctrl.getAliasUsedByApplication().length > 0;
			}
			
			
		} ],
		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-managers.html"
	};
} ];
