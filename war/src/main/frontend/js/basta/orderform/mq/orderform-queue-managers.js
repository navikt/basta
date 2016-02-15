'use strict';

module.exports = [ 'FasitService', function(FasitService) {
	
	 // Trick to always get an array. Xml2json will make one item arrays into an object
    function toArray(obj) {
        return [].concat(obj);
    }

	function extractAliases(values) {
		return _.chain(toArray(values))
			.map(function(value) {
				return value.alias;
			})
			.uniq()
			.value();
	}
	
	function extractUsedBy(values) {
		return _.chain(toArray(values))
			.map(function(value) {
				return value.usedby;
			})
			.flatten()
			.uniq()
			.value();
	}

	function transformQM(qmList) {
//		console.log(qmList)
		return _.chain(toArray(qmList))
		.groupBy("url")
		.map(function(values, key) {
			var first=values[0];
			return {
				'url' : key,
				'alias' :extractAliases(values),
				'name' : first.name,
				'hostname' : first.hostname,
				'port' : first.port,
				'usedby' : extractUsedBy(values),
			}
		}).value();
	}

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
				ctrl.choices=[];
				_.each(data, function(value, key) {
					ctrl.choices[key]= transformQM(data[key]);
				});
				console.log(ctrl.choices);
			});

			function setBestGuess() {
				var bestGuess = ctrl.choices[ctrl.envClassKey].filter(function(qm) {
					return ctrl.isUsedByApplication(qm);
				});
				if (bestGuess.length > 0) {
					ctrl.model = bestGuess[0].url;
				}

			}

			this.orderByUsed = function(qm) {
				if (ctrl.isUsedByApplication(qm)) {
					return 1;
				}
				return 100;
			}

			this.isUsedByApplication = function(qm) {
				return qm && qm.usedby.indexOf(ctrl.application) != -1;
			}

			$scope.$on("UpdateQueueManangerEvent", function(event, e) {
				// console.log("event", e);
				setBestGuess();
			})

		} ],
		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-managers.html"
	};
} ];
