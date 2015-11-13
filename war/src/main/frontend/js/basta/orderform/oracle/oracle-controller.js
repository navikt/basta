'use strict';

var angular = require('angular');

module.exports = ['BastaService', function (BastaService) {

    this.data = {
        nodeType: 'DB_ORACLE',
        environmentClass: 'u',
        environmentName: null,
        zone: 'fss',
        applicationName: null
    }

    this.submitOrder = function () {
        BastaService.submitOrderWithUrl('rest/db/orders/oracle', this.data);
    };

}];
