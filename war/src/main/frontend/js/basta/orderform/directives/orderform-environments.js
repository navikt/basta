'use strict';

module.exports =['FasitService', function (FasitService) {

        return {
            restrict: 'E',
            scope: {
                data :'=model',
                showValidation: "=",
                onSelect :'&',
                envClassKey:'=',
                config:'='
            },
            controller: function () {
        	var vm= this
                FasitService.environments.then( function(data){
                    vm.choices=data
                });
                	
                this.onSelectedItem = function(item){
                     this.config = item.multisite;
                }

            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-environments.html"
        };
    }];

