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
            required: '=',
            onChange: '&'
        },

        controller: ["$scope", function ($scope) {
            var vm = this;
            this.validate = function () {

                if (vm.invalidValues) {
                    var values = _.flatten(_.map(vm.internal, _.values));
                    var invalids = _.intersection(values, vm.invalidValues);
                    if (!_.isEmpty(invalids)) {
                        vm.subForm.$setValidity('required', false);
                        this.validationMessage = vm.invalidValuesText + ': ' + invalids;
                        selectInvalidDOMElements(invalids, values);
                    } else if (_.isEmpty(vm.internal)) {
                        vm.subForm.$setValidity('required', false);
                        this.validationMessage = "Angi minst en contextroot.";
                    }else {
                        vm.subForm.$setValidity('required', true);
                        delete this.validationMessage;
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

            function selectInvalidDOMElements(invalids, values){
                _.each(invalids, function(invalid){
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
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-multivalue.html"
    };
}];

