'use strict';

module.exports =['FasitService', function (FasitService) {


        var updateChoices = function (data) {
            this.choices = data;
        };
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
                FasitService.environments.then(updateChoices.bind(this))


                this.onSelectedItem = function(item){
                     this.config = item.multisite;
                }

            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/directives/orderform-environments.html"
        };
    }];

