'use strict';

module.exports = ["$timeout", function ($timeout) {

    return {
        restrict: 'E',
        scope: {
            internal: '=',
            model: '=',
            invalidValues: '=',
            invalidValuesText: '@',
            label: '@',
            placeholder: '@',
            maxLength: "@",
            minLength: '@',
            pattern: "@",
            showValidation: "=",
            required: '='
        },

        controllerAs: 'ctrl',
        controller: ["$scope", function ($scope) {

            var vm = this;

            this.validate = function () {

                var values = _.flatten(_.map(vm.internal, _.values));

                if (vm.invalidValues) {
                    console.log("invalid values");
                    var invalids = _.intersection(values, vm.invalidValues);
                    if (!_.isEmpty(invalids)) {
                        vm.subForm.$setValidity('required', false);
                        vm.validationMessage = vm.invalidValuesText + ': ' + invalids;
                        selectInvalidDOMElements(invalids, values);
                    } else {
                        vm.subForm.$setValidity('required', true);
                        console.log("set valid to true");
                        delete vm.validationMessage;
                    }
                }
            };

            this.updatemodel = function (a) {
                $timeout(function () {
                    vm.validate();
                    vm.model = _.flatten(_.map(vm.internal, _.values)).join();
                }, 10);
            };

            function selectInvalidDOMElements(invalids, values) {
                _.each(invalids, function (invalid) {
                    angular.element((angular.element('.tag-item'))[_.indexOf(values, invalid)]).addClass("selected");
                });
            }

            $scope.$watch(
                function () {
                    return vm.invalidValues;
                },
                function () {
                    vm.updatemodel();
                }
            );

        }],
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-multivalue.html"
    };
}];

