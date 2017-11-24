'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$routeParams', function (BastaService, $routeParams) {

    this.data = {
        nodeType: 'PLAIN_LINUX',
        environmentClass: $routeParams.environmentClass || 'p',
        zone: $routeParams.zone || 'fss',
        cpuCount: $routeParams.cpuCount || 1,
        memory: $routeParams.memory || 1,
        serverCount: $routeParams.serverCount || 1,
        osType: 'rhel70',
        disabledEnvClasses: ''
    }

    this.disableEnv = function () {
        switch (this.data.zone) {
            case 'iapp':
                this.data.environmentClass = 't'
                this.data.disabledEnvClasses = 'u'
                break;
            case 'fss':
                this.data.environmentClass = 'u'
                this.data.disabledEnvClasses = ''
                break;
            case 'sbs':
                this.data.environmentClass = 'u'
                this.data.disabledEnvClasses = ''
                break;
        }
    }

    this.submitOrder = function () {
        if (this.data.environmentClass === "q") {
            this.data.environmentName = "q1"
        }
        console.log("creating new order", this.data);

        BastaService.submitOrderWithUrl('rest/vm/orders/linux', this.data);
    };

}];
