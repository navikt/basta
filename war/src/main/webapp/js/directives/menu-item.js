'use strict';

angular.module('basta.menu-item', [])
    .directive('menuItem', ['$location',function ($location) {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: "partials/menu-item.html",
            scope:{
                header: '=',
                description: '=',
                image: '=',
                orderKey: '='
            },
            controllerAs: "menuController",
            controller: function(){
                this.newOrder = function(key){
                    console.log(key);
                    $location.url('vm_order?orderType='+key);

                }
            }
        };

    }]);

