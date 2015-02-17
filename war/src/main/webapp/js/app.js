'use strict';

// Declare app level module which depends on filters, and services
angular.module('basta', [
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'toggle-switch',
    'timer',
    'ui-rangeSlider',
    'ui.select',
    'angular-bootstrap-select',
    'angular-bootstrap-select.extra',
    'basta.error_service',
    'basta.notification_service',
    'basta.main_controller',
    'basta.error_controller',
    'basta.order_form_controller',
    'basta.decommision_form_controller',
    'basta.notifications_controller',
    'basta.order_list_controller',
    'basta.order_details_controller',
    'basta.changelog_controller',
    'basta.fasit_resource',
    'basta.ace_editor',
    'basta.orderdetails-panel',
    'basta.orderdetails-header',
    'basta.icon-circle',
    'changelogMarkdown'

])
    .config(['$routeProvider',
        function ($routeProvider) {
            $routeProvider.when('/vm_order', {
                templateUrl: 'partials/order_form.html',
                controller: 'orderFormController'
            });
            $routeProvider.when('/sten_order', {
                templateUrl: 'partials/order_form.html',
                controller: 'orderFormController'
            });
            $routeProvider.when('/decommision', {
                templateUrl: 'partials/decommision_form.html',
                controller: 'decommisionFormController'
            });
            $routeProvider.when('/notifications', {
                templateUrl: 'partials/notifications.html',
                controller: 'notificationsController'
            });
            $routeProvider.when('/order_list', {
                templateUrl: 'partials/order_list.html',
                controller: 'orderListController'
            });
            $routeProvider.when('/order_details/:id', {
                templateUrl: 'partials/order_details.html',
                controller: 'orderDetailsController'
            });
            $routeProvider.when('/changelog', {
                templateUrl: 'partials/changelog.html',
                controller: 'changelogController'
            });
            $routeProvider.otherwise({
                redirectTo: '/order_list'
            });
        }])
    .filter('timeago', function () {
        return function (date) {
            return moment(date).fromNow();
        }
    })
    .filter('humanize', function () {
        return function (string) {
            return _(string).chain().humanize().titleize().value();
        }
    }).factory('accessChecker', function () {
        return {
            hasEnvironmentClassAccess: function ($scope, environmentClass) {
                if ($scope.currentUser) {
                    var classes = $scope.currentUser.environmentClasses;
                    return classes.indexOf(environmentClass) > -1;
                }
                return false;
            },
            isLoggedIn: function (user) {
                return (!_.isUndefined(user) && user.authenticated);
            }
        }
    });

