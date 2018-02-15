'use strict';


module.exports = ['FasitService', function (FasitService) {
    var updateChoices = function (data) {
        this.choices = data;
    };
    return {
        restrict: 'E',
        scope: {
            data: '=model',
            showValidation: "=",
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
        templateUrl: "basta/orderform/directives/orderform-applications.html"
    };
}];