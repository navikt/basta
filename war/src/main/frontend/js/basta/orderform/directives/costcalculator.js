'use strict';

module.exports = [function () {


    return {
        restrict: 'E',
        scope: {
            cpus: '=',
            memory: '=',
            disk: '=',
            os: '@',
            environment: '=',
            environmentName: '=',
            vmType: '@?',
            classification: '=',
            serverCount: '='
        },

        controller: function () {
            var vm = this;
            var priceList = {
                esx: 176,
                blade: 344 / 3,
                memory: 280 / 3,
                backup: 1250,
                cpu: 479 / 3,
                disk: 32 / 3,
                disk_linux_template: 56,
                disk_windows_template: 80,
                rhel_p: 590,
                rhel_utq: 400,
                puppet: 750,
                windows: 0,
                ibm_scapm_p: 20500,
                ibm_scapm_utq: 10300,
                was: 5850,
                bpm: 23400,
                jboss: 1172,
                standard: 0,
                custom: 4500,
                srm: 2
            }
            var scapmEnvironments =  ['q0', 'q1', 'q3', 't3'];

            var summarize = function (total, num) {
                return isNaN(num) ? total : total + num
            }

            var calculateDisk = function (vmType, extra, os) {
                switch (vmType, os){
                    // linuxTemplate
                    case 'jboss':
                        // alle jboss, was, bpm og openam-servere får 20gb på /app
                        return (20 + extra + priceList.disk_linux_template) * priceList.disk;
                    case 'was':
                    case 'liberty':
                    case 'bpm':
                        // alle was og bpm får i tillegg 25gb på /opt
                        return (45 + extra + priceList.disk_linux_template) * priceList.disk;
                    case 'win':
                        return (priceList.disk_windows_template + extra) * priceList.disk;
                    case 'rhel':
                        return (priceList.disk_linux_template + extra) * priceList.disk;
                    default:
                        return (0 + extra) * priceList.disk;
                }
            }

            var calculateOs = function (os, env){
                if (os === 'rhel') {
                    if (env == "p") {
                        return priceList.rhel_p;
                    } else {
                        return priceList.rhel_utq;
                    };
                } else if (os === 'win') {
                    return 1000;
                }
            };

            var calculateType = function (type) {
                if (type == 'jboss'){
                    return priceList.jboss;
                } else if (type == 'was' || type == 'liberty') {
                    return priceList.was;
                } else if (type == 'bpm'){
                    return priceList.bpm;
                } else {
                    return 0;
                }

            }

            var calculateScapm = function (env){
                if (env == 'p') {
                    return priceList.ibm_scapm_p;
                } else {
                    return priceList.ibm_scapm_utq;
                }
            }


            this.unitCosts = function(){
                var cost =  {}
                cost.esx = priceList.esx;
                cost.blade = priceList.blade;
                cost.backup = priceList.backup;
                cost.cpu = priceList.cpu * vm.cpus;
                cost.memory = priceList.memory * vm.memory;
                cost.disk = calculateDisk(vm.vmType, vm.disk || 0, vm.os);
                cost.os = calculateOs(vm.os, vm.environment);
                if (vm.os == 'rhel' && (vm.environment == "p" || vm.environment == "q")){
                    cost.puppet = priceList.puppet;
                }
                if (vm.vmType) {
                    cost[vm.vmType] = calculateType(vm.vmType);
                    if ((vm.vmType === 'was' || vm.vmType ==='bpm') && scapmEnvironments.indexOf(vm.environmentName) > -1 ){
                        cost.scapm = calculateScapm(vm.environment)
                    }
                }
                if (vm.classification){
                    if (vm.classification.type === 'custom' || vm.classification === 'custom') {
                        cost.custom = priceList.custom;
                    }
                }

                _.each(cost, function(v, k){
                    cost[k] *=  vm.serverCount || 1;
                })
                return cost
            }

            this.estimatedPrice = function () {
                return _.values(this.unitCosts()).reduce(summarize, 0)

            }

        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/costcalculator.html"
    };
}];
