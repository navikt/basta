'use strict';

module.exports = ['FasitService', function (FasitService) {

    return {
        restrict: 'E',
        scope: {
            data: '=model',
            showValidation: "=",
            onSelect: '&',
            envClassKey: '=',
            customMultisiteAlert: '=',
            disableMultisiteAlert: '@'
        },
        controller: function () {
            var vm = this;
            FasitService.environments.then(function (data) {
                vm.choices = data;
            });
            
            if (angular.isUndefined(vm.disableMultisiteAlert)) {
				vm.disableMultisiteAlert = false;
			}
            
            this.isMultiSite = function (name) {
                return !vm.disableMultisiteAlert && FasitService.isMultiSite(vm.envClassKey, name);
            }
        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-environments.html"
    };
}];

