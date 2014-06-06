'use strict';

// Declare app level module which depends on filters, and services
angular.module('skyBestApp', [
    'ngResource',
    'ngRoute',
    'toggle-switch',
    'timer',
    'skyBestApp.error_service',
    'skyBestApp.main_controller',
    'skyBestApp.error_controller',
    'skyBestApp.order_form_controller',
    'skyBestApp.decommision_form_controller',
    'skyBestApp.order_list_controller',
    'skyBestApp.order_details_controller',
    'skyBestApp.node_list_controller',
    'skyBestApp.fasit_resource',
    'skyBestApp.ace_editor'
    ])
  .config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/order', {
            templateUrl: 'partials/order_form.html',
            controller: 'orderFormController'
        });
        $routeProvider.when('/decommision', {
            templateUrl: 'partials/decommision_form.html',
            controller: 'decommisionFormController'
        });
        $routeProvider.when('/order_list', {
          templateUrl: 'partials/order_list.html',
          controller: 'orderListController'
        });
        $routeProvider.when('/node_list', {
          templateUrl: 'partials/node_list.html',
          controller: 'nodeListController'
        });
        $routeProvider.when('/order_details/:id', {
            templateUrl: 'partials/order_details.html',
            controller: 'orderDetailsController'
        });
        $routeProvider.otherwise({
            redirectTo: '/order_list'
        });
    }]);

