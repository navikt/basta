'use strict';

module.exports = ['$location',function ($location) {
        return {
            restrict: 'E',
            operationText: '=',
            templateUrl: "basta/order-menu/menu-item.html",
            scope:{
                header: '=',
                description: '=',
                image: '=',
                orderKey: '=',
                formUrl:'='	
            },
            controllerAs: "menuController",
            controller: ['$scope', function($scope){
                this.newOrder = function(key){
                    if($scope.formUrl){
                    	$location.url($scope.formUrl);
                    }else if (_.chain(defaults).keys().contains(key).value()){
                        $location.url('vm_order?orderType='+key);
                    }else{
                        console.log("Not implemented yet");
                    }
                }
            }]
        };
    }];

