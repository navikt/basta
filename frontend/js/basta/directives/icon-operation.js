'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: 'js/basta/directives/icon-operation.html',
            scope:{
                operationText: '='
            }
        };
    };

