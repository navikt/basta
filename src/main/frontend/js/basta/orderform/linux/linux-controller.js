'use strict';

var angular = require('angular');

module.exports = [ 'BastaService','$routeParams', function(BastaService, $routeParams) {

    this.data = {
		nodeType : 'PLAIN_LINUX',
		environmentClass: $routeParams.environmentClass || 'u',
		zone: $routeParams.zone || 'fss',
		cpuCount: $routeParams.cpuCount || 1,
		memory: $routeParams.memory || 1,
		serverCount: $routeParams.serverCount || 1,
		osType:'rhel70'
    }


    this.submitOrder = function() {
        if (this.data.environmentClass === "q"){
            this.data.environmentName = "q1"
        }
    	console.log("creating new order", this.data);

		BastaService.submitOrderWithUrl('rest/vm/orders/linux', this.data);
    };

} ];
