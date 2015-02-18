'use strict';

angular.module('basta.menu-item', [])
    .directive('menuItem', function () {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: "partials/menu-item.html",
            scope:{
                header: '=',
                description: '=',
                image: '='
            }
        };
    });

