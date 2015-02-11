'use strict';

angular.module('skyBestApp.notifications_controller', [])
    .controller('notificationsController',
    ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', 'errorService','notificationService',
        function ($scope, $rootScope, $http, $routeParams, $resource, $location, errorService, notificationService) {

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

            var NotificationResource = $resource('rest/system/notifications');
            function getAll() {
                NotificationResource.query().$promise.then(
                    function (value) {
                        $scope.notifications = value;
                    }
                );
            }

            $scope.msg = "";


            $scope.$watch('test.message', function (newVal, oldVal) {
                if (newVal != oldVal){
                    console.log(newVal);

                }
            });

            getAll();



            $scope.setInactive = function(note){
                $http.put('rest/system/notifications/'+ note.id + '/inactive').success(function () {
                    getAll();
                    $rootScope.$broadcast('notification:updated',note);
                });
            };


            $scope.ModalController = function ($scope) {


                function clear(){
                    $scope.$parent.msg=null;
                }

                $scope.actions = {
                    INFO: {
                        'header': 'Information notification',
                        'message': 'By pressing OK, you will add the following information notification system wide to Basta',
                        'url': 'rest/system/notifications/create/info'

                    },
                    BLOCK: {
                        'header': 'Blocking notification',
                        'message': 'By pressing OK, you will add the following blocking notification system wide to Basta, preventing users to create new orders',
                        'url': 'rest/system/notifications/create/blocking'
                    }
                }

                $scope.$watch('msg', function (newVal, oldVal) {
                    if (newVal != oldVal){
                        $scope.message = newVal;
                    }
                });

                $scope.$watch('operation', function (newVal) {
                    if (!_.isUndefined(newVal)) {
                        $scope.header = $scope.actions[$scope.operation].header;
                        $scope.intro = $scope.actions[$scope.operation].message + ":";
                        $scope.url = $scope.actions[$scope.operation].url;
                    }
                });

                $scope.ok = function () {
                            $http.post($scope.url, $scope.message).success(function (result) {
                                clear();
                                getAll();
                                $rootScope.$broadcast('notification:updated',result);
                            }).error(errorService.handleHttpError($scope.header, 'notifications'));
                };
                $scope.cancel = function(){
                        clear();
                }
            };
        }]);
