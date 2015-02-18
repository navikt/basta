'use strict';

angular.module('basta.order_menu_controller', [])
    .controller('orderMenuController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', '$q', 'accessChecker', 'notificationService',
        function ($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache, $q, accessChecker, notificationService) {


        $scope.newOrderOfType = function(key){
            console.log(key);
        };


    }]);
