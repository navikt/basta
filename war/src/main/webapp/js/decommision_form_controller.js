'use strict';

angular.module('skyBestApp.decommision_form_controller', [])
    .controller('decommisionFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', 'errorService', function ($scope, $rootScope, $http, $routeParams, $resource, $location, errorService) {

        $scope.superUser = false;

        function retrieveUser() {
            $resource('/rest/users/:identifier').get({identifier: 'current'}, function (data) {
                $scope.currentUser = data;
            }).$promise.then(function () {
                    if (!$scope.currentUser.superUser) {
                        $location.path('/');
                    } else {
                        $scope.superUser = true;
                    }
                });
        }

        retrieveUser();

        $scope.$on('UserChanged', retrieveUser);



        $scope.ModalController = function ($scope) {

            $scope.actions ={
                values : [
                    {
                        'header':'Start',
                        'message':'Er du sikker på at du ønsker å starte ',
                        'url':'rest/nodes/decommission'
                    },
                    {
                        'header':'Stop',
                        'message':'Er du sikker på at du ønsker å starte ',
                        'url':'rest/nodes/decommission'
                    },
                    {
                        'header':'Avbestill',
                        'message':'Er du sikker på at du ønsker å dekommisjonere ',
                        'url':'rest/nodes/decommission'
                    }
                ],
                selected:'',
                busy:true
            };

            $scope.actions = {
                START: {
                    'header':'Start',
                    'message':'Er du sikker på at du ønsker å starte ',
                    'url':'rest/nodes/start'
                },
                STOP: {
                    'header':'Stop',
                    'message':'Er du sikker på at du ønsker å stoppe ',
                    'url':'rest/nodes/stop'

                },
                DECOMMISSION: {
                    'header':'Avbestill',
                    'message':'Er du sikker på at du ønsker å avbestille ',
                    'url':'rest/nodes/decommission'

                }
            }


            $scope.$watch('decommisionTarget', function () {
                $scope.target = $scope.decommisionTarget;
            });

            $scope.$watch('operation', function (newVal) {
                if (!_.isUndefined(newVal)){
                    $scope.header =$scope.actions[$scope.operation].header;
                    $scope.message =$scope.actions[$scope.operation].message + " " + $scope.target + "?";
                    $scope.url =$scope.actions[$scope.operation].url;
                }


        });

            $scope.ok = function () {
                $("#modal").modal('hide').on('hidden.bs.modal', function () {
                    $http.post($scope.url, $scope.decommisionTarget.split(',')).success(function (result) {
                        $location.path('/order_details/'+ result.orderId);
                    }).error(errorService.handleHttpError('Dekommisjonering', 'orderSend'));
                });
            };
        };
    }]);
