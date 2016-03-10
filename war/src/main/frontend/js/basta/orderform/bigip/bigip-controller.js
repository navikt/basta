'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$http', function (BastaService, $http) {

    this.data = {
        nodeType: 'LB_BIGIP',
        environmentClass: 'u',
        environmentName: null,
        zone: 'fss',
        application: null
    }


    this.clearData = function () {
        this.validation = null;
        this.data.environmentName=null;
    }

    this.changeEnvironmentClass = function () {
        this.clearData();
        delete this.data.environmentName;
        delete this.data.application;
    }

    this.changeZone = function () {
        this.updateTemplates(this.data.environmentClass, this.data.zone)
    }


    this.validateBigIP = function() {
        $http.get('rest/v1/bigip/validate', {params : this.data}
        ).success(function (data) {
            this.validation = data;
        }.bind(this))
    }


    this.submitOrder = function () {
        BastaService.submitOrderWithUrl('rest/v1/bigip', this.data);
    };

}];
