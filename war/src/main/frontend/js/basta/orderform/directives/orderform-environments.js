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
        	
        	this.isMultiSite = function(){
        	    var multiSites = [ 'q0', 'q1', 'q3', 'p' ];
        	    return _.contains(multiSites, vm.data);
        	}

            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-environments.html"
        };
    }];

