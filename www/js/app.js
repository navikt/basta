'use strict';

// Declare app level module which depends on filters, and services
angular.module('skyBestApp', [
    'ngResource',
    'ui.bootstrap',
    'ui.select2',
    'skyBestApp.controllers',
    ]).
config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: 'partials/intro.html',
            //controller: 'introController' // Have all its stuff in mainController, since we need it in the menu as well
        });
        $routeProvider.when('/about', {
            templateUrl: 'partials/about.html',
            //controller: 'aboutController'
        });
        $routeProvider.when('/template', {
            templateUrl: 'partials/order_form.html',
            controller: 'orderFormController',
        });
        $routeProvider.when('/:id', {
            templateUrl: 'partials/order_form.html',
            controller: 'orderFormController',
        });
        $routeProvider.otherwise({
            redirectTo: '/'
        });
    }
]);
