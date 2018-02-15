


'use strict';

var angular = require('angular');

module.exports = ['$scope', 'User', "BastaService", "FasitService", '$routeParams', function ($scope, User, BastaService, FasitService, $routeParams) {


    this.choices = {
        memory: [2, 3, 4, 6, 8, 10, 12, 14, 16],
        serverCount_standard: [1, 2, 3, 4, 5, 6, 7, 8],
        serverCount_multisite: [2, 4, 6, 8, 10, 12, 14, 16],
    }

    this.settings = {
        classification: {
            type: 'standard'
        }
    }

    this.data = {
		nodeType : 'JBOSS',
		environmentClass : $routeParams.environmentClass || 'u',
		zone : $routeParams.zone || 'fss',
		applicationMappingName : $routeParams.applicationMappingName,
		environmentName : $routeParams.environmentName,
		cpuCount : $routeParams.cpuCount || 2,
		serverCount : $routeParams.serverCount || 1,
		memory : $routeParams.memory || 2,
		extraDisk : $routeParams.extraDisk,
		eapVersion : $routeParams.eapVersion || 'EAP6',
		javaVersion : $routeParams.javaVersion || 'OpenJDK7',
		classification : $routeParams.classification
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
    
    this.isEap7 = function() {
        if (vm.data.eapVersion == "EAP7") {
            return true;
        } else {
        	return false} 
        }
        
    this.changeJavaVersion = function () {
        if (vm.data.eapVersion == "EAP7") {
            vm.data.javaVersion = "OpenJDK8";
        } 
        	
    }

    this.submitOrder = function () {
        this.data.classification = vm.settings.classification.type;
        this.data.description = vm.settings.classification.description;
        console.log("creating new jboss order", this.data);
        BastaService.submitOrderWithUrl('rest/vm/orders/jboss', this.data);
    };

}];
