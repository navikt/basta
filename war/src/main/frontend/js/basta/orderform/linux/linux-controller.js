'use strict';

var angular = require('angular');

module.exports = [ 'BastaService', function( BastaService) {

	this.data = {
		nodeType : 'PLAIN_LINUX',
		environmentClass : 'u',
		zone : 'fss',
		properties : {
			"cpuCount" : "1",
			"memory" : "1024",
			"serverCount" : "1"
		}
	}
	this.editData;

	this.submitOrder = function() {
		console.log("creating new order", this.data);
		BastaService.submitOrderWithUrl('rest/vm/orders/linux', this.data);
	};
	
} ];
