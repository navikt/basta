'use strict';

module.exports = [ '$http', 'errorService', 'BastaService', "$rootScope", '$routeParams', '$filter',
		function($http, errorService, BastaService, $rootScope, $routeParams, $filter) {

			this.data = {
				environmentClass : $routeParams.environmentClass || 'u',
				environmentName : $routeParams.environmentName,
				application : $routeParams.application,
				queueManager : $routeParams.queueMananger,
				mqQueueName : undefined,
				fasitResource : undefined,
			}

			this.queueNames = [];
			this.mqObjects = undefined;
			this.fasitResoures = [];
			var ctrl = this;

			function init() {
				updateQueueMananger();
			}

			function getQueueNames() {
				if (!ctrl.data.queueManager) {
					console.log("Queuemanager is not set. Resetting queuename choices")
					ctrl.queueNames = [];
					return;
				}
				$http.get("rest/v1/mq/queuenames", {
					'params' : {
						environmentClass : ctrl.data.environmentClass,
						queueManager : ctrl.data.queueManager
					},
					cache : true
				}).then(function(response) {
					ctrl.queueNames = response.data;
					// return $filter('filter')(response.data, filterby);
				}, function errorCallback(response) {
					console.log("error getting queueNames status", response.status, "data:", response.data)
				});
			}

			function getMqQbjects() {
				$http.get("rest/v1/mq/queue", {
					'params' : {
						environmentClass : ctrl.data.environmentClass,
						queueManager : ctrl.data.queueManager,
						queueName : ctrl.data.mqQueueName
					},
					cache : true
				}).then(function(response) {
					console.log("queue", response.data);
					ctrl.mqObjects = response.data;
					// return $filter('filter')(response.data, filterby);
				}, function errorCallback(response) {
					console.log("error getting queueNames status", response.status, "data:", response.data)
				});
			}

			this.queueNameSelected = function() {
				getFasitQueues(ctrl.data.environmentClass, ctrl.data.mqQueueName).then(function(data) {
					ctrl.fasitResources = data;
				});
				getMqQbjects();

			}

			// Trick to always get an array. Xml2json will make one item arrays
			// into an
			// object
			function toArray(obj) {
				return [].concat(obj);
			}

			function createQueueObject(item) {
				// console.log(item)
				var obj = {
					'id' : item.id,
					'alias' : item.alias,
					'environmentClass' : item.environmentClass,
					'environmentName' : item.environmentName,
					'queueName' : item.properties.filter(function(i) {
						return i.name === 'queueName';
					})[0].value,

					'usedby' : _.map(item.usedInApplication, function(app) {
						return app.name
					}),
				};
				return obj;
			}

			function getFasitQueues(environmentClass, queueName) {
				var fasitLookup = $http({
					method : 'GET',
					url : 'api/helper/fasit/resources',
					params : {
						type : "Queue",
						envClass : environmentClass,
					},
					cache : true
				});
				return fasitLookup.then(function onSuccess(response) {
					return _.chain(toArray(response.data))
					.map(createQueueObject)
					.filter(function(item) {
						// console.log("response", item.queueName);
						return item.queueName === queueName || item.queueName === "QA." + queueName;
					}).value();
				});
			}

			this.changeApplication = function() {
				updateQueueMananger();
			}

			this.changeEnvironmentClass = function() {
				delete this.data.environmentName;
				delete this.data.queueManager;
				updateQueueMananger()
			}

			this.changeEnvironment = function() {
				updateQueueMananger();
			}

			this.changeQueueManager = function() {
				getQueueNames();
			}

			function updateQueueMananger() {
				if (ctrl.data.environmentName && ctrl.data.application) {
					$rootScope.$broadcast('UpdateQueueManangerEvent', ctrl.data.queueManager);
					getQueueNames();
				}
			}

			this.start = function() {
				console.log("starting", ctrl.data);
				 BastaService.putOrder('rest/v1/mq/order/queue/start',ctrl.data);
			};

			this.stop = function() {
				console.log("stopping", ctrl.data);
				 BastaService.putOrder('rest/v1/mq/order/queue/stop',ctrl.data);
			};

			this.remove = function() {
				console.log("deleting", ctrl.data);
				 BastaService.putOrder('rest/v1/mq/order/queue/remove',ctrl.data);
			};

			init();

		} ];
