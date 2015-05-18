'use strict';

var angular = require('angular');

module.exports = ['$scope',  'BastaService', function ( $scope, BastaService ) {

       
        this.data={
            nodeType: 'PLAIN_LINUX',
            environmentClass: 'u',
            zone:'fss',
            "properties": {
                "disks": "0",
                "cpuCount": "1",
                "memory": "1024",
                "serverCount": "1"
            }
        }


        this.validate = function(data) {
            if($scope.form.$valid){
                this.master = angular.copy(data);
            };

        };

        this.submitOrder= function(){
       	 console.log("creating new order", this.data)
       	 BastaService.submitOrderWithUrl('rest/vm/orders/linux', this.data);
        };
        
    }];

