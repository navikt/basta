'use strict';

angular.module('skyBestApp.decommision_form_controller', [])
    .controller('decommisionFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', function ($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache) {

        $scope.ModalController = function($scope) {
            $scope.header = 'Dekommisjonering';
            $scope.message = 'Er du sikker på at du ønsker å dekommisjonere ' + $scope.decommisionTarget;

            $scope.ok = function() {
                alert('oki!');
            };
            $scope.cancel = function() {
                $('#modal').modal('hide');
            };
        };

        $scope.decommission = function() {
            $('#modal').modal('show');
        };

    }]);
