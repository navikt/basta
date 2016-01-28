'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

	this.data = {
		environmentClass : 'u',
		environmentName : null,
		application : undefined,
		queueManager : undefined,
		name : undefined,
		mqQueueName: null, 
		maxMessageSize : 4,
		queueDepth : 5000
	}
	
	this.generated={
			
	}

	var ctrl = this;

	this.generateQueueName = function() {
		var n='';
		if (ctrl.data.name)
			n = ctrl.data.name.toUpperCase().replace(/[^A-Z0-9._]/g, '');
		var e = '';
		if (ctrl.data.environmentName)
			e = ctrl.data.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + '_';
		var a = '';
		if (ctrl.data.application)
			a = ctrl.data.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + '_';
		
		return  e + a + n;
	}

	this.submitOrder = function() {
		console.log("Posting mq queue order", this.data)
		BastaService.submitOrderWithUrl('rest/orders/mq/queue', this.data);
	};

} ];
