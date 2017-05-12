'use strict';

module.exports = [ '$http', 'errorService', 'BastaService', "$rootScope", '$routeParams',
           		function($http, errorService, BastaService, $rootScope, $routeParams) {

	this.data = {
			environmentClass : $routeParams.environmentClass || 'u',
			environmentName : $routeParams.environmentName,
			application : $routeParams.application,
			queueManager : $routeParams.queueMananger,
			mqChannelName : $routeParams.mqChannelName,
			fasitAlias : $routeParams.fasitAlias,
			enableTls : false,
	}

	this.creates = [];

	this.inEditChannelNameMode = false;
	this.inEditFasitAliasMode=false;

	var ctrl = this;
	
	function init() {
		generateFasitAlias();
		generateName();
		updateQueueMananger();
	}
	
	this.changeEnvironment = function() {
		generateName();
		updateQueueMananger();
	}
	

	this.changeApplication = function() {
		generateFasitAlias();
		generateName();
		updateQueueMananger();
	}

	this.changeEnvironmentClass = function() {
		delete this.data.environmentName;
		delete this.queueManager;
		generateName();
	}
	
	this.changeFasitAlias = function() {
		resetValidation();
	}
	
	this.changeChannelName = function() {
		resetValidation();
	}

	function generateFasitAlias(){
		if (ctrl.inEditFasitAliasMode) {
			console.log("Will not generate new fasit alias in editmode");
			return;
		}
		resetValidation();
		ctrl.data.fasitAlias =  ctrl.data.application +"_channel";
	}
	
	function updateQueueMananger() {
		if (ctrl.data.environmentName && ctrl.data.application) {
			$rootScope.$broadcast('UpdateQueueManangerEvent', ctrl.data.queueManager);
		}
	}
	

	function generateName() {
		if (ctrl.inEditChannelNameMode) {
			console.log("Will not generate new channelname in editmode");
			return;
		}
		resetValidation();
		var env = '';
		if (ctrl.data.environmentName)
			env = ctrl.data.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + "_";
		var app = '';
		if (ctrl.data.application)
			app = ctrl.data.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') ;

		ctrl.data.mqChannelName = env + app ;
	}
	
	function resetValidation() {
		delete ctrl.validation;
	}


	function validate(){
		return $http.put('rest/v1/mq/order/channel/validate', ctrl.data)
			.then(function success(response) {
				console.log("validation ok");
				resetValidation();
			});
	}

	function sendOrder() {
		
		validate().then(function(){
			console.log("Posting mq queue order", ctrl.data)
			BastaService.postOrder('rest/v1/mq/order/channel', ctrl.data);
		}, function validationError(response){
			console.error("We have validation error ", response);
			if (response.status===409){
				ctrl.validation=response.data;
			}else{
				errorService.handleHttpError('Validation');
			}
			ctrl.processing=false;
		})
	}

	this.submitOrder = function() {
		ctrl.processing=true
		sendOrder();
	};
	
	init();

} ];
