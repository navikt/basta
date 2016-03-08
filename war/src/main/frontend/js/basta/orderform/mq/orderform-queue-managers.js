'use strict';

module.exports = [ '$q', '$http', function($q, $http) {

	var queueManangers = [];

	// Trick to always get an array. Xml2json will make one item arrays into an
	// object
	function toArray(obj) {
		return [].concat(obj);
	}

	function createQueueManagerObject(item) {
		// console.log(item)
		var obj = {
			'alias' : item.alias,
			'environmentClass' : item.environmentClass,
			'environmentName' : item.environmentName,
			'name' : item.properties.filter(function(i) {
				return i.name === 'name';
			})[0].value,
			'hostname' : item.properties.filter(function(i) {
				return i.name === 'hostname';
			})[0].value,
			'port' : item.properties.filter(function(i) {
				return i.name === 'port';
			})[0].value,
			'usedby' : _.map(item.usedInApplication, function(app) {
				return app.name
			}),
		};
		obj['url'] = 'mq://' + obj.hostname + ":" + obj.port + "/" + obj.name;

		return obj;
	}

	function extractAliases(values) {
		return _.chain(toArray(values)).pluck('alias').uniq().value();
	}

	function extractQueueManangerList(fasitList) {
		return _.chain(toArray(fasitList)).map(createQueueManagerObject).groupBy('url').map(function(group) {
			var item = _.first(group)
			item.alias = extractAliases(group)
			return item;
		}).value();
	}

	function getQueueManagers(environmentClass, environmentName, application) {
		var fasitLookup = $http({
			method : 'GET',
			url : 'api/helper/fasit/resources',
			params : {
				type : "QueueManager",
				bestmatch : true,
				envClass : environmentClass,
				envName : environmentName,
				app : application
			},
			cache : true
		});
		return fasitLookup.then(function onSuccess(response) {
			return extractQueueManangerList(response.data);
		});
	}

	function extractUsedBy(fasitResources) {
		return _.chain(toArray(fasitResources)).map(function(item) {
			return _.pluck(item.usedInApplication, 'name');
		}).flatten().uniq().value();
	}

	function createAliasApplicationMap() {
		var u = $http({
			method : 'GET',
			url : 'api/helper/fasit/resources?type=QueueManager&envClass=u&usage=true',
			cache : true
		});
		var t = $http({
			method : 'GET',
			url : 'api/helper/fasit/resources?type=QueueManager&envClass=t&usage=true',
			cache : true
		});
		var q = $http({
			method : 'GET',
			url : 'api/helper/fasit/resources?type=QueueManager&envClass=q&usage=true',
			cache : true
		});
		var p = $http({
			method : 'GET',
			url : 'api/helper/fasit/resources?type=QueueManager&envClass=p&usage=true',
			cache : true
		});
		return $q.all([ u, t, q, p ]).then(function onSuccess(response) {
			var allQms = [].concat(response[0].data, response[1].data, response[2].data, response[3].data);
			return _.chain(allQms).groupBy(function(qm) {
				return qm.alias;
			}).map(function(values, key) {
				// console.log(key, values);
				var obj = {
					alias : key,
					usedby : extractUsedBy(values)
				}
				return obj;
			}).value();

		});
	}

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
			
			createAliasApplicationMap().then(function(data) {
				console.log("alias application mapping", data);
				ctrl.aliasApplicationMap = data;
				updateChoices();
			});
			
			function updateChoices(){
//				console.log("updating", ctrl.environmentName, ctrl.application);
				if (ctrl.envClassKey && ctrl.environmentName && ctrl.application) {
					getQueueManagers(ctrl.envClassKey, ctrl.environmentName, ctrl.application).then(function(data) {
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
