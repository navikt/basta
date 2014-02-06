'use strict';

// Declare app level module which depends on filters, and services
var app = angular.module('skyBestApp', [
    'ngResource',
    'ngRoute',
    'ui.bootstrap',
    'ui.select2',
    'skyBestApp.main_controller',
    'skyBestApp.error_controller',
    'skyBestApp.order_form_controller',
    'skyBestApp.order_list_controller',
    'skyBestApp.node_list_controller',
    ])
  .config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/order', {
            templateUrl: 'partials/order_form.html',
            controller: 'orderFormController',
        });
        $routeProvider.when('/order_list', {
          templateUrl: 'partials/order_list.html',
          controller: 'orderListController',
        });
        $routeProvider.when('/node_list', {
          templateUrl: 'partials/node_list.html',
          controller: 'nodeListController',
        });
        $routeProvider.otherwise({
            redirectTo: '/order_list'
        });
    }])