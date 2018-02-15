'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: 'basta/directives/icon-operation.html',
            scope:{
                operationText: '='
            }
        };
    };

