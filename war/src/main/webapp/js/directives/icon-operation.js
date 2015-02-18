'use strict';

angular.module('basta.icon-operation', [])
    .directive('iconOperation', function () {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: "partials/icons/icon-operation.html",
            scope:{
                operationText: '='
            }
        };
    });

