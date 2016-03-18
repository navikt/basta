'use strict';

var Topic= require('./topic');

module.exports = [ '$http', 'errorService', 'BastaService', "$rootScope", '$routeParams', '$filter',
		function($http, errorService, BastaService, $rootScope, $routeParams, $filter) {

			this.data = {
				environmentClass : $routeParams.environmentClass || 'u',
				environmentName : $routeParams.environmentName,
				application : $routeParams.application,
				queueName : $routeParams.queueName,
				queueManager : undefined,
				fasitAlias : undefined,
				topicString : "",
				topicName : undefined,
			}
			this.creates = [];
			this.topics = [ '1', '2' ];
			this.validation = {};
			this.inEditQueueNameMode = false;
			var ctrl = this;

			function init() {
				generateFasitAlias();
				resetValidation();
				generateTopicString();
				updateQueueMananger();
			}

			this.changeApplication = function() {
				generateFasitAlias();
				resetValidation();
				generateTopicString();
				updateQueueMananger();
			}

			this.changeEnvironmentClass = function() {
				delete this.data.environmentName;
				delete this.data.queueManager;
				resetValidation();
				generateTopicString();
				updateQueueMananger()
			}

			this.changeEnvironment = function() {
				resetValidation();
				generateTopicString();
				updateQueueMananger();
			}

			this.changeTopicString = function() {
				generateFasitAlias();
				resetValidation();
			}

			this.changeFasitAlias = function() {
				resetValidation();
				generateTopicString();
			}

			this.changeMqQueueName = function() {
				resetValidation();
			}

			this.changeQueueManager = function() {
			}
			
			this.changeTopicString = function() {
				console.log("change topic")
				generateFasitAlias();
			}

			function updateQueueMananger() {
				if (ctrl.data.environmentName && ctrl.data.application) {
					$rootScope.$broadcast('UpdateQueueManangerEvent', ctrl.data.queueManager);
				}
			}

			function generateTopicString() {
				// ctrl.data.topicString = ctrl.data.environmentName + "/" ;
			}
			
			function generateTopicName() {
				// ctrl.data.topicString = ctrl.data.environmentName + "/" ;
			}

			function generateFasitAlias() {
				if (ctrl.inEditFasitAliasMode) {
					console.log("Will not generate new fasitAlias in editmode");
					return;
				}
				var alias=ctrl.data.application
				var topicString = ctrl.data.topicString;
				if(topicString && topicString.indexOf('/')!==-1){
					alias= alias + '_' + _.last(topicString.split("/"));
				}
				ctrl.data.fasitAlias = alias;
			}
			
			function getTopics() {
				if (!ctrl.data.queueManager) {
					console.log("no queuemanager")
					return [];
				}
				return $http.get("rest/v1/mq/topics", {
					params : {
						environmentClass : ctrl.data.environmentClass,
						queueManager : ctrl.data.queueManager
					},
					cache : true
				}).then(function(response) {
					return response.data;
				});
			}


			this.getTopicStrings = function(searchVal) {
				return getTopics().then(function(data) {
					var topics = _.chain(data)
					.filter(function(topicObj) {
						return Topic.matches(topicObj.topicString, searchVal);
					})
					.map(function(topicObj) {
						var topic= new Topic(topicObj.topicString);
						return topic.rightTrunc(searchVal);
					})
					.unique()
					.value();
					console.log("topics", topics);
					return topics;
				});
			}

			function hasValidationError() {
				return ctrl.validation.fasit || hasMqValidationError();
			}

			function resetValidation() {
				ctrl.validation = {};
			}

			this.sendOrder = function() {
				if (hasValidationError()) {
					console.log("We have validation errors", ctrl.validation)
				} else {
					console.log("Posting mq queue order", ctrl.data)
					BastaService.postOrder('rest/v1/mq/order/topic', ctrl.data);
				}
			}

			this.submitOrder = function() {
				ctrl.sendOrder();

			};

			init();

		} ];
