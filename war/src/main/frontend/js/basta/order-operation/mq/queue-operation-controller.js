'use strict';

module.exports = [ '$http', 'errorService', 'BastaService',  "$rootScope",'$routeParams','$filter', 
		function($http, errorService,  BastaService, $rootScope, $routeParams, $filter) {

			this.data = {
				environmentClass :  $routeParams.environmentClass || 'u',
				environmentName :  $routeParams.environmentName,
				application :  $routeParams.application,
				queueManager : $routeParams.queueMananger,
			}
			
			this.names=["alla","balla", "calla"];
			var ctrl = this;
			
			function init(){
				updateQueueMananger();
			}
			
			this.getQueueNames= function(val){
				console.log("view", val);
				return $http.get("rest/v1/mq/queuenames", {
					'params' : ctrl.data,
					cache : true
				}).then(function(response) {
					return $filter('filter')(response.data, val);
				}, function errorCallback(response) {
					console.log("error getting clusters status", response.status, "data:", response.data)
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
			}

			function updateQueueMananger() {
				if (ctrl.data.environmentName && ctrl.data.application) {
					$rootScope.$broadcast('UpdateQueueManangerEvent', ctrl.data.queueManager);
				}
			}


			this.start = function() {
				var hostnames = extractHostnames();
				console.log("starting", hostnames);
//				BastaService.createOrderNoFlatmap('rest/vm/operations/start', hostnames);
			};

			this.stop = function() {
				var hostnames = extractHostnames();
				console.log("stopping", hostnames);
//				BastaService.createOrderNoFlatmap('rest/vm/operations/stop', hostnames);
			};

			this.remove = function() {
				var hostnames = extractHostnames();
				console.log("deleting", hostnames);
//				BastaService.createOrderNoFlatmap('rest/vm/operations/decommission', hostnames);
			};
			
			init();

		} ];
