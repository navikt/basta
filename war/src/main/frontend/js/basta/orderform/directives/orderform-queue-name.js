'use strict';

module.exports = [function () {
       
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                label: '=',
                showValidation: "=",
                required: '=',
                mqName: '@',
                mqAlias: '@',
                mqBoq: '@',
                environmentName: '=',
                application: '='
            },
            link: function(scope) {

            	scope.mqNames = function() {
            		var n = '';
            		if(scope.ctrl.data != null) n = scope.ctrl.data.toUpperCase().replace(/[^A-Z0-9._]/g, '');
            		var e = '';
            		if(scope.ctrl.environmentName != null) e = scope.ctrl.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '')+'_';
            		var a = '';
            		if(scope.ctrl.application != null) a = scope.ctrl.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '')+'_';
            		scope.ctrl.mqName = e+a+n;
            		scope.ctrl.mqAlias = 'QA.'+scope.ctrl.mqName;
            		scope.ctrl.mqBoq = scope.ctrl.mqName+'.BOQ';
            	};
            	
        	    scope.$watch('ctrl.environmentName', scope.mqNames);
        	    scope.$watch('ctrl.application', scope.mqNames);

            },
            controller: function () {
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-queue-name.html"
        };
    }];

