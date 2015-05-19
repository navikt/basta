'use strict';

module.exports = [ function () {

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                showValidation: "=",
                choices: "=",
                onSelect: '&onSelect'
            },

            controller:  ['$scope',function ($scope) {
//            	console.log($scope.choices);
            	 $scope.choices= [1,2,3,4,6,8];
            }],
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-cpucount.html"
        };
    }];

