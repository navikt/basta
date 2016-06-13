'use strict';

var _ = require('underscore');

module.exports = ['BastaService', '$http', '$scope', '$timeout', '$routeParams', function (BastaService, $http, $scope, $timeout, $routeParams) {

    require('../../utils/util').initTooltips();

    this.data = {
        environmentClass: $routeParams.environmentClass || 'u',
        environmentName: $routeParams.environmentName,
        zone: $routeParams.zone || 'fss',
        application: $routeParams.application,
        virtualserver: $routeParams.virtualserver,
        contextroots: $routeParams.contextroots,
        hostname: $routeParams.hostname,
        useHostnameMatching: $routeParams.useHostnameMatching || false,
    };

    this.changeEnvironmentClass = function () {
        delete this.data.environmentName;
        this.baseInputChanged()
    };

    this.baseInputChanged = function () {
        delete this.data.virtualserver;
        delete this.virtualservers;
        delete this.validation;
        if (this.baseInputIsComplete()) {
            this.getVirtualServers()
        }
    };

    this.baseInputIsComplete = function () {
        return (this.data.environmentClass && this.data.environmentName && this.data.zone && this.data.application);
    };

    this.toggleMatchingType = function () {
        this.data.useHostnameMatching = !this.data.useHostnameMatching;
        delete this.missingHostname
    };

    this.validate = function (formdata, callback) {
        $http.get('rest/v1/bigip/validate', {params: formdata}
        ).success(function (data) {
            this.validation = data;
            this.validation.invalidValues = _.flatten(_.map(data.conflictingContextRoots, _.values));

            if (callback) {
                callback.bind(this)(data)
            }
            $scope.form.$submitted = true
        }.bind(this))
    };

    this.getVirtualServers = function () {
        $http.get('rest/v1/bigip/virtualservers', {params: this.data})
            .then(
                function (response) {
                    delete this.validation;
                    this.virtualservers = response.data
                }.bind(this),

                function () {
                    this.validation = {virtualServerMissing: true};
                    this.virtualservers = []
                }.bind(this)
            )
    };

    this.onHostnameChange = function () {
        delete this.missingHostname
    };

    this.submitOrder = function () {
        this.processing = true;

        // frontend validation
        if (this.data.useHostnameMatching && !this.data.hostname) {
            this.missingHostname = true
        } else {
            delete this.missingHostname
        }

        this.validate(this.data, function () {
            var vm = this;
            // wrapping the $valid check within timeout ensures that at least one digest loop has occurred
            $timeout(function () {
                if ($scope.form.$valid) {
                    BastaService.submitOrderWithUrl('rest/v1/bigip', vm.data)
                } else {
                    vm.processing = false
                }
            }, 500)
        })
    }
}];
