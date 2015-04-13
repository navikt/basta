'use strict';

module.exports = function () {
        return {
            restrict: 'E',
            statusText: '=',
            templateUrl: 'js/basta/directives/icon-status.html',
            scope:{
                statusText: '='
            }
        };
    };

