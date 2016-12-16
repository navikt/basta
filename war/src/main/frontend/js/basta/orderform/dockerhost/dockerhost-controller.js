'use strict';

var angular = require('angular');

module.exports = ['$scope', 'User', "BastaService", "FasitService", '$routeParams', function ($scope, User, BastaService, FasitService, $routeParams) {

    this.choices = {
        memory: [2, 3, 4, 6, 8, 10, 12, 14, 16],
        serverCount_standard: [1, 2, 3, 4, 5, 6, 7, 8],
        serverCount_multisite: [2, 4, 6, 8, 10, 12, 14, 16],
        clusterNames: []
    }

    this.data = {
        nodeType: 'DOCKERHOST',
        environmentClass: $routeParams.environmentClass || 'u',
        zone: $routeParams.zone || 'fss',
        clusterName: $routeParams.clusterName,
        environmentName: $routeParams.environmentName,
        cpuCount: $routeParams.cpuCount || 2,
        serverCount: $routeParams.serverCount || 1,
        memory: $routeParams.memory || 2,
        extraDisk: $routeParams.extraDisk,
        classification: "custom"
    }

    this.choices.serverCount = this.choices.serverCount_standard;
    var vm = this;

    function isMultiSite() {
        return FasitService.isMultiSite(vm.data.environmentClass, vm.data.environmentName);
    }

    this.changeEnvironmentClass = function () {
        delete this.data.environmentName;
    }

    this.changeEnvironment = function () {
        // update list of clusternames
        FasitService.clusters(this.data.environmentName).then(function (response) {
            console.log(response.data)
            this.choices.clusterNames = response.data
        }.bind(this))

        if (isMultiSite()) {
            vm.choices.serverCount = vm.choices.serverCount_multisite;
        } else {
            vm.choices.serverCount = vm.choices.serverCount_standard;
        }
        if (!_(vm.choices.serverCount).contains(vm.data.serverCount)) {
            console.log("Illegal choice for servercount. Clearing data")
            delete vm.data.serverCount
        }
    }

    this.submitOrder = function () {
        this.data.classification = vm.settings.classification.type;
        this.data.description = vm.settings.classification.description;
        console.log("creating new jboss order", this.data);
        BastaService.submitOrderWithUrl('rest/vm/orders/jboss', this.data);
    };

}];
