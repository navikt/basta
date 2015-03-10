'use strict';

angular.module('basta.orderform-environmentclasses', [])
    .directive('orderformEnvironmentClasses', ['User', 'accessChecker', function (User, accessChecker) {
        return {
            restrict:'E',
            scope: {
                environmentClasses: '&',
                environmentClassNames: '&'
            },
            controller: function () {
                this.environmentClasses =  ['u', 't', 'q', 'p'];
                this.environmentClassNames = {u: 'Utvikling', t: 'Test', q: 'PreProd', p: 'Produksjon'};
                User.currentUser().$promise.then(
                    function(data){
                        console.log("HOI");
                        console.log(data);
                    }
                )



                function retrieveUser() {
                    $resource('/rest/users/:identifier').get({identifier: 'current'}, function (data) {
                        return data;
                    });
                };
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-environmentclasses.html"
        };

    }]);

