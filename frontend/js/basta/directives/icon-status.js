'use strict';

module.exports = function () {
        return {
            restrict: 'E',
            statusText: '=',
            templateUrl: 'basta/directives/icon-status.html',
            scope:{
                statusText: '='
            }
        };
    };

