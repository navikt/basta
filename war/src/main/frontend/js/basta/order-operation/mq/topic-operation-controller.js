'use strict';

module.exports = [ '$http', 'errorService', 'BastaService', '$routeParams', 'queueManagerService',	function($http, errorService, BastaService, $routeParams, queueManagerService) {

			this.data = {
				environmentClass : $routeParams.environmentClass || 'u',
				queueManager : $routeParams.queueManager,
				topicString : $routeParams.topicString,
			}
			this.queueManangers = [];
			this.topics = [];
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
					updateTopics()
				});
			}

			function findInArray(array, value){
				return _.find(array, function(item){
					return item.toUpperCase().indexOf(item.toUpperCase()) !==-1;
				})
			}


			function updateTopics() {
				if (!ctrl.data.queueManager) {
					console.log("Queuemanager is not set. Resetting queuename choices")
					ctrl.queueNames = [];
					return;
				}
				$http.get("rest/v1/mq/topics", {
					'params' : {
						environmentClass : ctrl.data.environmentClass,
						queueManager : ctrl.data.queueManager
					},
					cache : false
				}).then(function(response) {
					ctrl.topics = response.data;

					updateValidation()
				}, function errorCallback(response) {
					console.log("error getting queueNames status", response.status, "data:", response.data)
				});
			}


			function updateValidation(){
				if(!ctrl.data.topicString){
					delete ctrl.fasitResources;
					return;
				}
				getFasitTopics(ctrl.data.environmentClass, ctrl.data.topicString)
				.then(function(data) {
					ctrl.fasitResources = data;
				});
			}


			// Trick to always get an array. Xml2json will make one item arrays
			// into an
			// object
			function toArray(obj) {
				return [].concat(obj);
			}

			function createTopicObject(item) {
				// console.log(item)
				var obj = {
					'id' : item.id,
					'alias' : item.alias,
					'environmentClass' : item.environmentClass,
					'environmentName' : item.environmentName,
					'topicString' : item.properties.filter(function(i) {
						return i.name === 'topicString';
					})[0].value,

					'usedby' : _.map(item.usedInApplication, function(app) {
						return app.name
					}),
				};
				return obj;
			}

			function getFasitTopics(environmentClass, topicString) {
				var fasitLookup = $http({
					method : 'GET',
					url : 'api/helper/fasit/resources',
					params : {
						type : "Topic",
						envClass : environmentClass,
					},
					cache : true
				});
				return fasitLookup.then(function onSuccess(response) {
					return _.chain(toArray(response.data))
					.map(createTopicObject)
					.filter(function(item) {
						 console.log("response", item.topicString);
						return item.topicString === topicString ;
					}).value();
				});
			}

			this.changeEnvironmentClass = function() {
				delete this.data.queueManager;
				updateQueueMananger()
			}

			this.changeQueueManager = function() {
				updateTopics();
			}

			this.topicSelected = function() {
				updateValidation();
			}

			this.start = function() {
				console.log("starting", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/topic/start', ctrl.data);
			};

			this.stop = function() {
				console.log("stopping", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/topic/stop', ctrl.data);
			};

			this.remove = function() {
				console.log("deleting", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/topic/remove', ctrl.data);
			};

			init();

		} ];
