'use strict';

module.exports = ['User', function (User) {

    function Controller() {
        var vm = this;

        User.onchange(function () {
            var user = User.current();
            vm.envClasses = enrichWithUserAccess(user);
            vm.envClasses = filterDisabled(vm.envClasses);
        });

        function filterDisabled(envClasses) {
            console.log("vm", vm)
            var disable_classes = vm.disable || "";
            return _.chain(envClasses).map(function (envClass) {
                envClass.isDisabled = disable_classes.indexOf(envClass.key) > -1;
                return envClass;
            }).value();
        }


        function enrichWithUserAccess(userData) {
            var classes = userData.environmentClasses;
            var classesWithNames = [{
                key: 'u',
                name: 'Utvikling'
            }, {
                key: 't',
                name: 'Test'
            }, {
                key: 'q',
                name: 'PreProd'
            }, {
                key: 'p',
                name: 'Produksjon'
            }];

            return _.chain(classesWithNames).map(function (envClass) {
                envClass.hasAccess = classes.indexOf(envClass.key) > -1;
                return envClass;
            }).value();
        }

    }

    return {
        restrict: 'E',
        scope: {
            data: '=model',
            disable: '=disabled',
            onSelect: '&onSelect'
        },
        controller: Controller,
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/orderform/directives/orderform-environmentclasses.html",
    };
}];
