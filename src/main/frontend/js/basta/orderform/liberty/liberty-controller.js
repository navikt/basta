'use strict';

var angular = require('angular');

module.exports = ['$scope', 'User', "BastaService", "FasitService", '$routeParams', function ($scope, User, BastaService, FasitService, $routeParams) {


    this.choices = {
        memory: [1, 2, 3, 4, 6, 8, 10, 12, 14, 16],
        serverCount_standard: [1, 2, 3, 4, 5, 6, 7, 8],
        serverCount_multisite: [2, 4, 6, 8, 10, 12, 14, 16]
    }

    this.settings = {
        classification: {
            type: 'standard'
        }
    }

    this.data = {
        nodeType: 'LIBERTY',
        environmentClass: $routeParams.environmentClass || 'u',
        zone: $routeParams.zone || 'fss',
        applicationMappingName: $routeParams.applicationMappingName || null,
        environmentName: $routeParams.environmentName || null,
        cpuCount: $routeParams.cpuCount || 1,
        serverCount: $routeParams.serverCount || 1,
        memory: $routeParams.memory || 1,
        extraDisk: $routeParams.extraDisk || null,
        classification: $routeParams.classification || null


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

    this.estimatedPrice = function () {
        var unitCost = 600 + 138 + this.data.cpuCount * 100 + this.data.memory * 400;
        if (vm.data.extraDisk) {
            unitCost = unitCost + vm.data.extraDisk * 32;
        }
        if (vm.settings.classification.type === 'custom') {
            unitCost = unitCost * 2;
        }

        return this.data.serverCount * unitCost;
    }

    this.submitOrder = function () {
        this.data.classification = vm.settings.classification.type;
        this.data.description = vm.settings.classification.description;
        console.log("Creating new WAS Liberty order", this.data);
        BastaService.submitOrderWithUrl('rest/vm/orders/liberty', this.data);
    };

}];
