'use strict';

var angular = require('angular');

module.exports = [ 'BastaService', function(BastaService) {

    this.data = {
	osType: 'win2012',
	middleWareType: 'windows_ap',
	environmentClass : 't',
	environment: null,
	zone : 'fss',
	cpuCount : 1,
	memory : 1,
	serverCount : 1
    }

    this.submitOrder = function() {
	console.log("creating new windows order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/windows', this.data);
    };

} ];
