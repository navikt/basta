'use strict';

module.exports = [ '$http', 'errorService', 'BastaService', '$routeParams', 'queueManagerService',	function($http, errorService, BastaService, $routeParams, queueManagerService) {

			this.data = {
				environmentClass : $routeParams.environmentClass || 'u',
				queueManager : $routeParams.queueManager,
				mqQueueName : $routeParams.mqQueueName,
			}
			this.queueManangers = [];
			this.queueNames = [];
			this.mqObjects = undefined;
			this.fasitResoures = [];
			var ctrl = this;

			function init() {
				updateQueueMananger();
			}

			function updateQueueMananger() {
				queueManagerService.getQueueManagers(ctrl.data.environmentClass).then(function(data) {
					console.log("queuemanagers", ctrl.data.environmentClass, data);
					ctrl.queueManagers = data;
					// reset selected
					if (_.pluck(ctrl.queueManagers, "url").indexOf(ctrl.data.queueManager) === -1) {
						console.log("Selected queuemananger not found, resetting",ctrl.data.queueManager );
						ctrl.data.queueManager = undefined;
					}
					updateQueueNames()
				});
			}

			function findInArray(array, value){
				return _.find(array, function(item){
					return item.toUpperCase().indexOf(item.toUpperCase()) !==-1;
				})
			}

 			function updateQueueNames() {
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
					cache : false
				}).then(function(response) {
					ctrl.queueNames = response.data;
					var foundQueue = findInArray(ctrl.queueNames, ctrl.data.mqQueueName);
					if(!foundQueue){
						console.log("Selected queue not found, resetting" , ctrl.data.mqQueueName );
					}
					ctrl.data.mqQueueName=foundQueue;
					updateValidation()
				}, function errorCallback(response) {
					console.log("error getting queueNames status", response.status, "data:", response.data)
				});
			}


			function updateValidation(){
				if(!ctrl.data.mqQueueName){
					delete ctrl.fasitResources;
					delete ctrl.mqObjects;
					return;
				}
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

			function getMqQbjects() {
				$http.get("rest/v1/mq/queue", {
					'params' : {
						environmentClass : ctrl.data.environmentClass,
						queueManager : ctrl.data.queueManager,
						queueName : ctrl.data.mqQueueName
					},
					cache : false
				}).then(function(response) {
					console.log("queue", response.data);
					ctrl.mqObjects = response.data;
				}, function errorCallback(response) {
					console.log("error getting queueNames status", response.status, "data:", response.data)
				});
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
					url : 'rest/v1/fasit/resources',
					params : {
						type : "Queue",
						envClass : environmentClass,
					},
					cache : true
				});
				return fasitLookup.then(function onSuccess(response) {
					return _.chain(toArray(response.data)).map(createQueueObject).filter(function(item) {
						// console.log("response", item.queueName);
						return item.queueName === queueName || item.queueName === "QA." + queueName;
					}).value();
				});
			}


			this.changeEnvironmentClass = function() {
				delete this.data.queueManager;
				updateQueueMananger()
			};

			this.changeQueueManager = function() {
				updateQueueNames();
			};

			this.queueNameSelected = function() {
				updateValidation();
			};

			this.start = function() {
				console.log("starting", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/queue/start', ctrl.data);
			};

			this.stop = function() {
				console.log("stopping", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/queue/stop', ctrl.data);
			};

			this.remove = function() {
				console.log("deleting", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/queue/remove', ctrl.data);
			};

			init();

		} ];
