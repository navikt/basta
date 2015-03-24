'use strict';

angular.module('basta.orderform-submit-buttons', [])
    .directive('orderformSubmitButtons', ['User','BastaService', function (User, BastaService) {
        var isSuperUser = function (superuser) {
            this.superuser = superuser;
        };

        return {
            restrict: 'E',
            scope: {
                data: '=model',
                onSelect: '&onSelect'
            },



            controller: function ($scope) {

                $scope.$on('UserUpdated', function(){
                    User.sudo().then(isSuperUser.bind(this));
                }.bind(this));

                this.submit = function(){

                }


                $scope.submitOrder = function () {

                    if ($scope.isValidForm()) {
                        setDisks();
                        $scope.settings.nodeType = $scope.nodeType;
                        $scope.orderSent = true;
                        $scope.busies.orderSend = true;
                        if ($scope.prepared && $scope.prepared) {
                            $http.put('rest/orders/' + $scope.prepared.orderId, $scope.prepared.xml, {
                                headers: {'Content-type': 'text/plain', 'Accept': 'application/json'}
                            }).success(onOrderSuccess).error(onOrderError);
                        } else {
                            $http.post('rest/orders',_.omit($scope.settings, 'sugar'))
                                .success(onOrderSuccess).error(onOrderError);
                        }
                    }
                };

            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/orderform-submit-buttons.html"
        };
    }]);

