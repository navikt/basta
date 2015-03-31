'use strict';

module.exports =  function () {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: "partials/icons/icon-operation.html",
            scope:{
                operationText: '='
            }
        };
    };

