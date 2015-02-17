'use strict';

angular.module('basta.icon-circle', [])
    .directive('iconCircle', function () {
        return {
            restrict: 'E',
            statusText: '=',
            templateUrl: "partials/icon-circle.html",
            scope:{
                statusText: '='
            }
        };
    });

