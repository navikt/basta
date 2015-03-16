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
                orderKey: '=',
                formUrl:'='	
            },
            controllerAs: "menuController",
            controller: function($scope){
                this.newOrder = function(key){
                	console.log($scope.formUrl)
                    if($scope.formUrl){
                    	$location.url($scope.formUrl);
                    }else if (_.chain(defaults).keys().contains(key).value()){
                        $location.url('vm_order?orderType='+key);
                    }else{
                        console.log("Not implemented yet");
                    }
                }
            }
        };
    }]);

