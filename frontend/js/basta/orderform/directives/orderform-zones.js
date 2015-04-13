'use strict';

module.exports = ['User', function (User) {
        var zones = [
            {
                key: 'fss',
                name: 'Fagsystemsone'
            },
            {
                key: 'sbs',
                name: 'Selvbetjeningsone'
            }];
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                showValidation: "=",
                onSelect: '&onSelect'

            },
            controller: function () {
               this.zones = zones;
                this.data = 'fss';
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "js/basta/orderform/directives/orderform-zones.html"
        };
    }];

