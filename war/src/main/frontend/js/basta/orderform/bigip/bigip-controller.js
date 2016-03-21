module.exports = ['BastaService', '$http', function (BastaService, $http) {

    this.data = {
        environmentClass: 'u',
        environmentName: null,
        zone: 'fss',
        application: null,
        virtualserver: null,
        contextroots: null
    }

    this.changeEnvironmentClass = function () {
        delete this.data.environmentName;
        this.inputChanged();
    }

    this.inputChanged = function () {
        delete this.data.virtualserver;
        delete this.virtualservers;
        delete this.validation;
        if (this.basicInfoIsSet()) {
            this.validate(this.data, this.getVirtualServers);
        }
    }

    this.onContextRootBlur = function () {
        this.validate(this.data)
    }

    this.basicInfoIsSet = function () {
        return (this.data.environmentClass && this.data.environmentName && this.data.zone && this.data.application);
    }

    this.validate = function (formdata, callback) {
        $http.get('rest/v1/bigip/validate', {params: formdata}
        ).success(function (data) {
            this.validation = data;
            if (callback) {
                callback.bind(this)(data)
            }
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
