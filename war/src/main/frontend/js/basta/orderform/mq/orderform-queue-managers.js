'use strict';

module.exports =['FasitService', function (FasitService) {

        return {
            restrict: 'E',
            scope: {
                data :'=model',
                showValidation: "=",
                onSelect :'&',
                envClassKey:'='
            },
            controller: function () {
        	var vm= this;
                FasitService.queueManagers().then( function(data){
                    vm.choices=data;
                });
        	
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/mq/orderform-queue-managers.html"
        };
    }];

