'use strict';

angular.module('basta.orderform-serversizes', [])
    .directive('orderformServersizes', ['BastaService', function (BastaService) {
        var updateChoices = function (data) {
            this.choices = data;
        };

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                onSelect: '&onSelect'
            },

            controller: function () {
               BastaService.serverSizes.then(updateChoices.bind(this));
                console.log(this);
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-serversizes.html"
        };
    }]);

