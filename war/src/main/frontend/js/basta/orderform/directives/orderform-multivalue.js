'use strict';

module.exports = ["$timeout", function ($timeout) {

    return {
        restrict: 'E',
        scope: {
            internal: '=',
            model: '=',
            invalidValues: '=',
            label: '@',
            placeholder: '@',
            maxLength: "@",
            minLength: '@',
            pattern: "@",
            showValidation: "=",
            required: '=',
            onChange: '&',
        },

        controller: ["$scope", function ($scope) {
            var vm = this;
            this.validate = function () {

                if (_.isEmpty(vm.internal)) {
                    this.validationMessage = "Angi minst en contextroot.";
                }
                console.log("validate calls!" + vm.invalidValues);
                if (vm.invalidValues) {
                    var values = _.flatten(_.map(vm.internal, _.values));
                    var invalid = _.intersection(values, vm.invalidValues);
                    if (!_.isEmpty(invalid)) {
                        vm.subForm.$setValidity('required', false);
                        this.validationMessage = 'Ugyldige verdier: ' + invalid;
                    } else {
                        vm.subForm.$setValidity('required', true);
                    }
                }
            };

            this.validate();


            this.updatemodel = function (a) {
                $timeout(function () {
                    vm.validate();
                    vm.model = _.flatten(_.map(vm.internal, _.values)).join();
                }, 10);

            };

            $scope.$watch(
                function () {
                    return vm.invalidValues;
                },
                function () {
                    vm.updatemodel();
                }
            );

        }],
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-multivalue.html"
    };
}];

