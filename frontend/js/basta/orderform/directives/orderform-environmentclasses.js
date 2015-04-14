'use strict';


module.exports = ['User', function (User) {
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
                onSelect: '&onSelect'
            },
            controller: ['$scope',function ($scope) {
                this.data = 'u';
                User.current().then(enrichWithUserAccess).then(updateEnvironmentClasses.bind(this));

                $scope.$on('UserUpdated', function(){
                    User.current().then(enrichWithUserAccess).then(updateEnvironmentClasses.bind(this));
                }.bind(this));
            }],
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "js/basta/orderform/directives/orderform-environmentclasses.html"
        };
    }];

