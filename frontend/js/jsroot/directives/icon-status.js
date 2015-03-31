'use strict';

module.exports = function () {
        return {
            restrict: 'E',
            statusText: '=',
            templateUrl: "partials/icons/icon-status.html",
            scope:{
                statusText: '='
            }
        };
    };

