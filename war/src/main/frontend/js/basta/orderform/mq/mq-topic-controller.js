'use strict';

var Topic = require('./topic');

module.exports = [ '$http', 'errorService', 'BastaService', "$rootScope", '$routeParams', '$q',
		function($http, errorService, BastaService, $rootScope, $routeParams, $q) {

			this.data = {
				environmentClass : $routeParams.environmentClass || 'u',
				environmentName : $routeParams.environmentName,
				application : $routeParams.application,
				queueManager : undefined,
				topicString : $routeParams.topicString,
				fasitAlias : undefined,
			}
			this.creates = [];
			this.validation = undefined;
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
				updateQueueMananger();
			}

			this.changeEnvironmentClass = function() {
				delete this.data.environmentName;
				delete this.data.queueManager;
				resetValidation();
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
			}

			this.changeMqQueueName = function() {
				resetValidation();
			}

			this.changeQueueManager = function() {
				resetValidation();
			}

			this.changeTopicString = function() {
// console.log("change topic")
				generateFasitAlias();
				resetValidation();
			}

			function updateQueueMananger() {
				if (ctrl.data.environmentName && ctrl.data.application) {
					$rootScope.$broadcast('UpdateQueueManangerEvent', ctrl.data.queueManager);
				}
			}

			function generateTopicString() {
				if (ctrl.data.environmentName) {
					var topic = new Topic(ctrl.data.topicString);
					ctrl.data.topicString = topic.replaceEnvWith(ctrl.data.environmentName);
				}
			}

			function generateTopicName() {
				// ctrl.data.topicString = ctrl.data.environmentName + "/" ;
			}

			function generateFasitAlias() {
				if (ctrl.inEditFasitAliasMode) {
					console.log("Will not generate new fasitAlias in editmode");
					return;
				}
				if (ctrl.data.topicString) {
					var topic = new Topic(ctrl.data.topicString);
					ctrl.data.fasitAlias = ctrl.data.application + "_" + topic.topicStringWithOutEnv(ctrl.data.environmentName);
				}
			}

			function getTopics() {
				if (!ctrl.data.queueManager) {
					console.log("no queuemanager")
					return $q(function(resolve, reject){
						resolve([]);
					});
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
						var topic = new Topic(topicObj.topicString);
						return topic.rightTrunc(searchVal);
					})
					.unique()
					.value();
					console.log("topics", topics);
					return topics;
				});
			}
			
			function resetValidation() {
				delete ctrl.validation;
			}
			
			
			function validate(){
				return $http.put('rest/v1/mq/order/topic/validate', ctrl.data)
					.then(function success(response) {
						console.log("validation ok");
						resetValidation();
					});
			}

			this.sendOrder = function() {
				validate().then(function(){
					console.log("Posting mq queue order", ctrl.data)
//					BastaService.postOrder('rest/v1/mq/order/topic', ctrl.data);
				}, function validationError(response){
					console.error("We have validation error ", response);
					if (response.status===409){
						ctrl.validation=response.data;
					}else{
						errorService.handleHttpError('Validation');
					}
				})
			}

			this.submitOrder = function() {
				ctrl.sendOrder();

			};

			init();

		} ];
