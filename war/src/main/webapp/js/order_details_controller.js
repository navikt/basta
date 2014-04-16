'use strict';

angular.module('skyBestApp.order_details_controller', [])
  .controller('orderDetailsController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$timeout','$rootScope',
        function($scope, $http, $resource, $routeParams, $location, $timeout,$rootScope) {

        $scope.exists=false;
        $scope.showXML=false;
        $scope.routeParamsId=$routeParams.id;

        var OrderResource  = $resource('rest/orders/:orderId', {orderId : '@id'});
        var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId : '@id'});

        OrderResource.get({orderId:$routeParams.id})
            .$promise.then(
                function ( value ){
                    $scope.exists = true;
                    $scope.orderDetails = value;
                },
                function ( error ){
                    $scope.exists = false;
                }
            )
        $scope.statusLog = OrderLogs.query({orderId:$routeParams.id});

        //Needed because Ace neeeds its data just in time.
        $scope.$on('toggled', function (f, value) {
                if (value){
                    $scope.xmlreq  = $scope.orderDetails.requestXml;
                }else{
                    $scope.xmlreq  = undefined;
                }
            });


  }]);