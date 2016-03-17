module.exports = ['BastaService', '$http', function (BastaService, $http) {

    this.data = {
        environmentClass: 'u',
        environmentName: null,
        zone: 'fss',
        application: null,
        virtualserver: null
    }


    this.changeEnvironmentClass = function () {
        delete this.data.environmentName;
        this.inputChanged();
    }



    this.inputChanged = function () {
        delete this.data.virtualserver;
        delete this.virtualservers;
        delete this.validation;
        if (this.isCompleteInput()){
            this.validateBigIP(this.getVirtualServers);
        }
    }


    this.isCompleteInput = function () {
        return (this.data.environmentClass && this.data.environmentName && this.data.zone && this.data.application);
    }

    this.validateBigIP = function (callback) {
        console.log("calling validate with", this.data)
        $http.get('rest/v1/bigip/validate', {params: this.data}
        ).success(function (data) {
            this.validation = data;
            callback.bind(this)(data)
        }.bind(this))
    }


    this.getVirtualServers = function (validation) {
        if (validation.bigIpResourceExists) {
            $http.get('rest/v1/bigip/virtualservers', {params: this.data}
            ).success(function (data) {
                this.virtualservers = data;
            }.bind(this))
        } else {
            console.log("validation said bigip was false")
        }
    }


    this.submitOrder = function () {
        BastaService.submitOrderWithUrl('rest/v1/bigip', this.data);
    };

}]
;

'use strict';

var angular = require('angular');
