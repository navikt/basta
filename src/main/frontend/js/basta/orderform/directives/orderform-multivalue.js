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

            vm.validationMessage = 'Må ha minimum en verdi';

            this.updatemodel = function (a) {
                vm.model = _.flatten(_.map(vm.internal, _.values)).join();
            };

            $scope.$watch(
                function () {
                    vm.updatemodel();
                }
            );

        }],
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-multivalue.html"
    };
}];

