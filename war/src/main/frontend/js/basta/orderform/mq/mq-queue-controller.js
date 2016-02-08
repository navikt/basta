'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', '$q', function($http, errorService, FasitService, BastaService, $q) {

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

	this.validation = {};

	this.inEditQueueNameMode = false;

	var ctrl = this;

	this.changeApplication = function() {
		if (!this.data.fasitAlias) {
			this.data.fasitAlias = this.data.application + "_";
		}
		resetValidation();
		this.generateQueueName();
	}

	this.changeEnvironmentClass = function() {
		delete this.data.environmentName;
		delete this.data.queueManager;
		resetValidation();
		this.generateQueueName();
	}

	this.changeFasitAlias = function() {
		resetValidation();
		this.generateQueueName();
	}
	
	this.changeQueueName= function(){
		resetValidation();
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

	// validate promise
	function validate(callback) {
		console.log("validating");
		if (ctrl.data.environmentClass && ctrl.data.environmentName && ctrl.data.application && ctrl.data.queueManager) {

			$http.put("rest/orders/mq/queue/validation", ctrl.data).then(function(response) {
				console.log("response", response.data);
				ctrl.validation = response.data;
				ctrl.validation.mqerror = hasMqValidationError();
				var mqDetails = []
				if (response.data.local_queue) {
					mqDetails.push("Lokal kø finnes allerede i MQ")
				}
				if (response.data.backout_queue) {
					mqDetails.push("Backout kø finnes allerede i MQ")
				}
				if (response.data.alias_queue) {
					mqDetails.push("Kø alias finnes allerede i MQ")
				}
				ctrl.validation.mqErrorDetails = mqDetails;

				callback()
			}, errorService.handleHttpError('Validation'));
		} else {
			console.log("noe er ikke satt")
			callback()
		}
	}

	function hasMqValidationError() {
		return ctrl.validation.local_queue || ctrl.validation.backout_queue || ctrl.validation.alias_queue
	}

	function hasValidationError() {
		return ctrl.validation.fasit || hasMqValidationError();
	}
	
	function resetValidation(){
		ctrl.validation = {};
	}

	this.sendOrder = function() {
		if (hasValidationError()) {
			console.log("We have validation errors", ctrl.validation)
		} else {
			console.log("Posting mq queue order", ctrl.data)
			// BastaService.submitOrderWithUrl('rest/orders/mq/queue', ctrl.data);
		}
	}

	this.submitOrder = function() {
		validate(ctrl.sendOrder);

	};

} ];
