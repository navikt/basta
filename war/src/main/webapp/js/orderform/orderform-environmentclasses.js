'use strict';

angular.module('basta.orderform-environmentclasses', [])
    .directive('orderformEnvironmentClasses',function (User) {
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
                onSelect: '&onSelect',
                user: '=user'

            },
            controller: function ($scope) {
                this.data = 'u';
                User.current().then(enrichWithUserAccess).then(updateEnvironmentClasses.bind(this));

                $scope.$on('UserUpdated', function(){
                    User.current().then(enrichWithUserAccess).then(updateEnvironmentClasses.bind(this));
                }.bind(this));
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-environmentclasses.html"
        };
    });

