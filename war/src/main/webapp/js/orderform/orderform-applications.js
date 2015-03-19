'use strict';

angular.module('basta.orderform-applications', [])
    .directive('orderformApplications', ['FasitService', function (FasitService) {
        var updateChoices = function (data) {
            this.choices = data;
        };
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                onSelect: '&onSelect',
                applicationsOnly: '='
            },

            controller: function () {
                this.applicationsOnly
                    ? FasitService.applications.then(updateChoices.bind(this))
                    : FasitService.all.then(updateChoices.bind(this));
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-applications.html"
        };
    }]);

