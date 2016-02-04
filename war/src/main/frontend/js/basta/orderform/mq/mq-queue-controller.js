'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

	this.data = {
		environmentClass : 'u',
		environmentName : null,
		application : undefined,
		queueManager : undefined,
		fasitAlias : undefined,
		mqQueueName : null,
		maxMessageSize : 4,
		queueDepth : 5000
	}

	this.creates = [];
	
	this.validation={};

	this.inEditQueueNameMode = false;

	var ctrl = this;

	this.changeApplication = function() {
		if (!this.data.fasitAlias) {
			this.data.fasitAlias = this.data.application + "_";
		}
		this.generateQueueName();
	}

	this.changeEnvironmentClass = function() {
		delete this.data.environmentName;
		delete this.queueManager;
		this.generateQueueName();
	}
	
	this.changeFasitAlias = function() {
		this.generateQueueName();
		validate();
	}

	this.generateQueueName = function() {
		if (this.inEditQueueNameMode) {
			console.log("Will not generate new queuename in editmode");
			return;
		}
		var env = '';
		if (ctrl.data.environmentName)
			env = ctrl.data.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + "_";
		var app = '';
		if (ctrl.data.application)
			app = ctrl.data.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + "_";
		var name = '';
		if (ctrl.data.fasitAlias) {
			name = ctrl.data.fasitAlias.toUpperCase().replace(/[^A-Z0-9._]/g, '');
			// fjerner appnavn om det står først også i fasit alias
			var removeAppNamePattern = new RegExp('^' + app + '');
			name = name.replace(removeAppNamePattern, '');
		}

		this.data.mqQueueName = env + app + name;
	}
	
	function validate(){
		console.log("validating");
		if(ctrl.data.environmentClass && ctrl.data.environmentName && ctrl.data.application && ctrl.data.queueManager){
			
			$http.put("rest/orders/mq/queue/validation", ctrl.data)
			.then(function(response){
				console.log("response", response.data);
				ctrl.validation=response.data;
				ctrl.validation.mqerror=response.data.local_queue ||response.data.backout_queue ||response.data.alias_queue
				var mqDetails=[]
				if(response.data.local_queue){
					mqDetails.push("Lokal kø finnes allerede i MQ")
				}
				if(response.data.backout_queue){
					mqDetails.push("Backout kø finnes allerede i MQ")
				}
				if(response.data.alias_queue){
					mqDetails.push("Kø alias finnes allerede i MQ")
				}
				ctrl.validation.mqErrorDetails=mqDetails;
				
			},errorService.handleHttpError('Validation'));
		}else{
			console.log("noe er ikke satt")
		}
	}

	this.submitOrder = function() {
		console.log("Posting mq queue order", this.data)
		BastaService.submitOrderWithUrl('rest/orders/mq/queue', this.data);
	};

} ];
