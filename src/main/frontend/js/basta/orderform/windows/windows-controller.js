'use strict';

var angular = require('angular');

module.exports = [ 'BastaService', '$routeParams', function(BastaService, $routeParams) {

    this.data = {
    		osType: $routeParams.osType || 'win2019',
    		middleWareType: $routeParams.middleWareType || 'windows_ap',
    		environmentClass: $routeParams.environmentClass || 't',
    		environment: $routeParams.environment || null,
    		zone: $routeParams.zone || 'fss',
    		cpuCount: $routeParams.cpuCount || 1,
    		memory: $routeParams.memory || 4,
    		serverCount: $routeParams.serverCount || 1
    }

    this.submitOrder = function() {
	console.log("creating new windows order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/windows', this.data);
    };

} ];
