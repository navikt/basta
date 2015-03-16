'use strict';

angular.module('basta.orderform-environmentclasses', [])
    .directive('orderformEnvironmentClasses', ['User', 'accessChecker', function (User, accessChecker) {
        var envClasses = [
            {
                key: 'u',
                name: 'Utvikling'
            },
            {
                key: 't',
                name: 'Test'
            },
            {
                key: 'q',
                name: 'PreProd'
            },
            {
                key: 'p',
                name: 'Produksjon'
            }];

        var enrichWithUserAccess = function (userData) {
            var classes = userData.environmentClasses;
            return _.chain(envClasses)
                .map(function (envClass) {
                    envClass.hasAccess = classes.indexOf(envClass.key) > -1;
                    return envClass;
                })
                .value();
        };

        var updateEnvironmentClasses = function (data) {
            this.envClasses = data;
        };

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                after: '&after'

            },
            controller: function () {
                User.currentUser().$promise
                    .then(enrichWithUserAccess)
                    .then(updateEnvironmentClasses.bind(this));

                this.data = 'u';

                this.updateModel = function (envClassKey) {
                    this.data = envClassKey;
                };
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-environmentclasses.html"
        };
    }]);

