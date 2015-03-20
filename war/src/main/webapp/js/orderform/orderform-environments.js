'use strict';

angular.module('basta.orderform-environments', [])
    .directive('orderformEnvironments', ['FasitService', function (FasitService) {
        var updateChoices = function (data) {
            this.choices = data;
        };
        return {
            restrict: 'E',
            scope: {
                data :'=model',
                onSelect :'&onSelect',
                envClassKey:'=envClassKey'
            },
            controller: function () {
                FasitService.environments.then(updateChoices.bind(this))
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-environments.html"
        };
    }]);

