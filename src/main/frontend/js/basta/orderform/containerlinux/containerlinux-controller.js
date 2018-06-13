'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$routeParams', function (BastaService, $routeParams) {

    this.data = {
        nodeType: 'LIGHTWEIGHT_LINUX',
        environmentClass: $routeParams.environmentClass || 'p',
        zone: $routeParams.zone || 'fss',
        cpuCount: $routeParams.cpuCount || 1,
        memory: $routeParams.memory || 1,
        serverCount: $routeParams.serverCount || 1,
        osType: 'coreos',
        ibmSw: false,
    }

    this.submitOrder = function () {
        console.log("creating new order", this.data);
        BastaService.submitOrderWithUrl('rest/vm/orders/containerlinux', this.data);
    };



}];
