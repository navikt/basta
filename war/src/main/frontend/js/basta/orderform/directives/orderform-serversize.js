'use strict';

module.exports = ['BastaService', function (BastaService) {
        var updateChoices = function (data) {
            this.choices = data;
        };

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                showValidation: "=",
                onSelect: '&onSelect'
            },

            controller: function () {
               BastaService.serverSizes.then(updateChoices.bind(this));
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-serversizes.html"
        };
    }];
