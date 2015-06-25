'use strict';

var angular = require('angular');

module.exports = [ 'BastaService', function(BastaService) {

    this.data = {
	nodeType : 'PLAIN_LINUX',
	environmentClass : 'u',
	zone : 'fss',
	cpuCount : 1,
	memory : 1,
	serverCount : 1
    }

    this.submitOrder = function() {
	console.log("creating new order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/linux', this.data);
    };

} ];
