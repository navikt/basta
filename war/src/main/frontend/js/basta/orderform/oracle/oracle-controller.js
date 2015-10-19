'use strict';

var angular = require('angular');

module.exports = ['BastaService', function (BastaService) {

    this.data = {
        nodeType: 'PLAIN_LINUX',
        environmentClass: 'u',
        environmentName: null,
        zone: 'fss',
        applicationMappingName: null
    }

    this.submitOrder = function () {
        console.log("creating new order", this.data);
        BastaService.submitOrderWithUrl('rest/vm/orders/oracle', this.data);
    };

}];
