'use strict';

angular.module('basta.orderform-zones', [])
    .directive('orderformZones', ['User', function (User) {
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
                after: '&after'

            },
            controller: function () {
               this.zones = zones;
                this.data = 'fss';
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-zones.html"
        };
    }]);

