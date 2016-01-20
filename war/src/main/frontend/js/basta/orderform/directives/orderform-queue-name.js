'use strict';

module.exports = [function () {
       
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                label: '=',
                showValidation: "=",
                required: '='
            },
            link: function($scope) {
            	$scope.mqName = function() {
            		if($scope.ctrl.data != null) $scope.ctrl.data = $scope.ctrl.data.toUpperCase().replace(/[^A-Z0-9._]/g, '');
            	}
            },
            controller: function () {
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-queue-name.html"
        };
    }];

