'use strict';

angular.module('skyBestApp.order_details_controller', [])
  .controller('orderDetailsController', ['$scope', '$http', '$resource', '$routeParams', '$location', '$timeout','$rootScope',
        function($scope, $http, $resource, $routeParams, $location, $timeout,$rootScope) {

        $scope.model ={
                exists: false,
                showXML : false,
                routeParamsId : $routeParams.id
            }

        var OrderResource  = $resource('rest/orders/:orderId', {orderId : '@id'});
        var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId : '@id'});

        OrderResource.get({orderId:$routeParams.id})
            .$promise.then(
                function ( value ){
                    $scope.model.exists = true;
                    $scope.orderDetails = value;
                },
                function ( error ){
                    $scope.model.exists = false;
                }
            )
        $scope.statusLog = OrderLogs.query({orderId:$routeParams.id});


        //Needed because Ace neeeds its data just in time.
        $scope.$watch('model.showXML', function (newVal, oldVal) {
            if (newVal){
                $scope.model.xmlreq  = $scope.orderDetails.requestXml;
            }else{
                $scope.model.xmlreq  = undefined;
            }
        });

  }]);