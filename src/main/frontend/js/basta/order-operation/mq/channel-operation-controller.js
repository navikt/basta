'use strict';

module.exports = [ '$http', 'errorService', 'BastaService', '$routeParams', 'queueManagerService',	function($http, errorService, BastaService, $routeParams, queueManagerService) {

			this.data = {
				environmentClass : $routeParams.environmentClass || 'u',
				queueManager : $routeParams.queueManager,
				mqChannelName : $routeParams.mqChannelName,
			}
			this.queueManangers = [];
			this.channels = [];
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
					updateChannels()
				});
			}


			function updateChannels() {
				if (!ctrl.data.queueManager) {
					console.log("Queuemanager is not set. Resetting queuename choices")
					ctrl.channels = [];
					return;
				}
				$http.get("rest/v1/mq/channels", {
					'params' : {
						environmentClass : ctrl.data.environmentClass,
						queueManager : ctrl.data.queueManager
					},
					cache : false
				}).then(function(response) {
					ctrl.channels = response.data;
					if(ctrl.data.mqChannelName && ! findChannel(ctrl.channels, ctrl.data.mqChannelName)){
//						console.log("resetting ");
						delete ctrl.data.mqChannelName
					}

					updateValidation()
				}, function errorCallback(response) {
					console.log("error getting channelname status", response.status, "data:", response.data)
				});
			}
			
			function findChannel(channels, channelName){
				return _.find(channels, function(channel){
					return channel===channelName;
				} );
			}


			function updateValidation(){
				if(!ctrl.data.mqChannelName){
					delete ctrl.fasitResources;
					return;
				}
				findInFasit(ctrl.data.environmentClass, ctrl.data.mqChannelName)
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

			function createFasitObject(item) {
				// console.log(item)
				var obj = {
					'id' : item.id,
					'alias' : item.alias,
					'environmentClass' : item.environmentClass,
					'environmentName' : item.environmentName,
					'channelName' : item.properties.filter(function(i) {
						return i.name === 'name';
					})[0].value,

					'usedby' : _.map(item.usedInApplication, function(app) {
						return app.name
					}),
				};
				return obj;
			}

			function findInFasit(environmentClass, channelName) {
				var fasitLookup = $http({
					method : 'GET',
					url : 'rest/v1/fasit/resources',
					params : {
						type : "Channel",
						envClass : environmentClass,
					},
					cache : true
				});
				return fasitLookup.then(function onSuccess(response) {
					return _.chain(toArray(response.data))
					.map(createFasitObject)
					.filter(function(item) {
//						 console.log("response", item.channelName);
						return item.channelName === channelName ;
					}).value();
				});
			}

			this.changeEnvironmentClass = function() {
				delete this.data.queueManager;
				updateQueueMananger()
			}

			this.changeQueueManager = function() {
				updateChannels();
			}

			this.channelSelected = function() {
				updateValidation();
			}

			this.start = function() {
				console.log("starting", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/channel/start', ctrl.data);
			};

			this.stop = function() {
				console.log("stopping", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/channel/stop', ctrl.data);
			};

			this.remove = function() {
				console.log("deleting", ctrl.data);
				BastaService.putOrder('rest/v1/mq/order/channel/remove', ctrl.data);
			};

			init();

		} ];
