'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$routeParams', function (BastaService, $routeParams) {

    console.log("in ofiac")

    this.data = {
        nodeType: 'DEV_TOOLS',
        environmentClass: 'p',
        zone: 'iapp',
        cpuCount: $routeParams.cpuCount || 1,
        memory: $routeParams.memory || 1,
        serverCount: $routeParams.serverCount || 1,
        osType: 'rhel70'
    }

    this.submitOrder = function () {
        console.log("creating new order", this.data);
        BastaService.submitOrderWithUrl('rest/vm/orders/devtools', this.data);
    };

}];
