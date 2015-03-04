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
                    if (_.chain(defaults).keys().contains(key).value()){
                        $location.url('vm_order?orderType='+key);
                    }else if (key==='adServiceUser' ){
                    	$location.url('ad_order?orderType='+key);
                    }else{
                        console.log("Not implemented yet");
                    }
                }
            }
        };
    }]);

