'use strict';



//require('jquery');

require('xml2json');


var angular = require('angular');


require('ui-select');



var basta = angular.module('basta', [
    require('angular-resource'),
    require('angular-route'),
    require('angular-sanitize'),
    'ui.select']);

require('./jsroot');

//// Declare app level module which depends on filters, and services
//angular.module('basta', [
//    'ngResource',
//    'ngRoute',
//    'ngSanitize',
//    'ui.select',
//    'ui.checkbox'
//])
    basta.config(['$routeProvider',
        function ($routeProvider) {
    	 	$routeProvider.when('/serviceuser_order',               { templateUrl: 'partials/serviceuser/serviceuser_order_form.html'});
    	 	$routeProvider.when('/serviceuser_certificate_order',   { templateUrl: 'partials/serviceuser/serviceuser_certificate_order_form.html'});
            $routeProvider.when('/vm_order',                        { templateUrl: 'partials/order_form_vm.html'});
            $routeProvider.when('/jboss_order',                     { templateUrl: 'partials/orderform/orderform-jboss.html'});
            $routeProvider.when('/menu',                            { templateUrl: 'partials/order_menu.html'});
            $routeProvider.when('/decommision',                     { templateUrl: 'partials/decommision_form.html'});
            $routeProvider.when('/notifications',                   { templateUrl: 'partials/notifications.html'});
            $routeProvider.when('/order_list',                      { templateUrl: 'partials/order_list.html'});
            $routeProvider.when('/order_details/:id',               { templateUrl: 'partials/order_details.html'});
            $routeProvider.when('/changelog',                       { templateUrl: 'partials/changelog.html'});
            $routeProvider.otherwise(                               { redirectTo: '/order_list'});
        }]);

    basta.filter('timeago', function () {
        return function (date) {
            return moment(date).fromNow();
        }
    });
    basta.filter('humanize', function () {
        return function (string) {
            return _(string).chain().humanize().titleize().value();
        }
    });




basta.factory('accessChecker', function () {
        return {
            hasEnvironmentClassAccess: function ($scope, environmentClass) {
                if ($scope.currentUser) {
                    var classes = $scope.currentUser.environmentClasses;
                    return classes.indexOf(environmentClass) > -1;
                }
                return false;
            },
            hasEnvClassAccess: function (environmentClass, currentUser) {
                  var classes = currentUser.environmentClasses;
                  return classes.indexOf(environmentClass) > -1;

            },

            isLoggedIn: function (user) {
                return (!_.isUndefined(user) && user.authenticated);
            }
        }
    });

