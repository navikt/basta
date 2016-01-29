'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

	this.data = {
		environmentClass : 'u',
		environmentName : null,
		application : undefined,
		queueManager : undefined,
		fasitAlias : undefined,
		mqQueueName: null, 
		maxMessageSize : 4,
		queueDepth : 5000
	}
	
	this.inEditQueueNameMode=false;

	var ctrl = this;
	
	this.changeApplication= function(){
		if(!this.data.fasitAlias){
			this.data.fasitAlias=this.data.application + "_";
		}
		this.generateQueueName();
	}

	this.generateQueueName = function() {
		console.log("generate", ctrl.data.fasitAlias);
		if(this.inEditQueueNameMode){
			console.log("Will not generate new queuename in editmode");
			return;
		}
		var name='';
		if (ctrl.data.fasitAlias)
			name = ctrl.data.fasitAlias.toUpperCase().replace(/[^A-Z0-9._]/g, '');
		var env = '';
		if (ctrl.data.environmentName)
			env = ctrl.data.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + "_";
		var app = '';
		if (ctrl.data.application)
			app = ctrl.data.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '')+ "_";
		
		var removeAppNamePattern= new RegExp('^' +app +'');
		name=name.replace(removeAppNamePattern, '');

		this.data.mqQueueName= env + app + name;
		console.log("generate done", ctrl.data.mqQueueName);
//		return ctrl.data.mqQueueName;
	}

	this.submitOrder = function() {
		console.log("Posting mq queue order", this.data)
		BastaService.submitOrderWithUrl('rest/orders/mq/queue', this.data);
	};

} ];
