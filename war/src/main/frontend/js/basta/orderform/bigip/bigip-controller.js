'use strict';

var _ = require('underscore');

module.exports = ['$http', '$location', '$scope', '$timeout', '$routeParams', function ($http, $location, $scope, $timeout, $routeParams) {

    require('../../utils/util').initTooltips();

    this.data = {
        environmentClass: $routeParams.environmentClass || 'u',
        environmentName: $routeParams.environmentName,
        zone: $routeParams.zone || 'fss',
        application: $routeParams.application,
        virtualserver: $routeParams.virtualserver,
        contextroots: $routeParams.contextroots,
        hostname: $routeParams.hostname,
        useHostnameMatching: $routeParams.useHostnameMatching === "true"
    };

    this.changeEnvironmentClass = function () {
        delete this.data.environmentName;
        this.baseInputChanged()
    };

    this.baseInputChanged = function () {
        delete this.data.virtualserver;
        delete this.virtualservers;
        if (this.baseInputIsComplete()) {
            this.getVirtualServers()
        }
    }

    this.baseInputIsComplete = function () {
        return (this.data.environmentClass && this.data.environmentName && this.data.zone && this.data.application);
    }

    this.toggleMatchingType = function () {
        this.data.useHostnameMatching = !this.data.useHostnameMatching;
        delete this.missingHostname
    }

    this.getVirtualServers = function () {
        $http.get('rest/v1/bigip/virtualservers', {params: this.data})
            .then(
                function (response) {
                    this.virtualservers = response.data
                }.bind(this),
                function () {
                    this.validation.virtualServerMissing = true
                    this.virtualservers = []
                }.bind(this)
            )
    }

    this.submitOrder = function () {
        this.processing = true;
        var vm = this

        $http.post('rest/v1/bigip', this.data).then(
            //success
            function (response) {
                var newOrderId = response.data
                $location.path('/order_details/' + newOrderId)
            },
            //error
            function (response) {
                vm.processing = false
                vm.errorMessage = "Error: " + response.data + ". HTTP " + response.status + " (" + response.statusText + ")"
            }
        )
    }
}];
