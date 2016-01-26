'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

    this.data= {
	environmentClass : 'u',
	environmentName: null,
	application : undefined,
	queueManager : undefined,
	queueName : undefined,
	maxMessageSize: 4,
	queueDepth: 5000
    }

    var ctrl = this;

    this.submitOrder = function() {
	console.log("Posting mq queue order", this.data)
	BastaService.submitOrderWithUrl('rest/orders/mq/queue', this.data);
    };

} ];
