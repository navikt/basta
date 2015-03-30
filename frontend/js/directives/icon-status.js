'use strict';

angular.module('basta.icon-status', [])
    .directive('iconStatus', function () {
        return {
            restrict: 'E',
            statusText: '=',
            templateUrl: "partials/icons/icon-status.html",
            scope:{
                statusText: '='
            }
        };
    });

