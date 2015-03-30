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
                formvalid: '=',
                onSelect: '&onSelect',
                formdata: '=formdata',
                submitEditedData: '='
            },


            controller: function ($scope) {
                $scope.$on('UserUpdated', function(){
                    User.sudo().then(isSuperUser.bind(this));
                }.bind(this));
                User.sudo().then(isSuperUser.bind(this));

                this.submit = function(){
                    if(this.formvalid){
                        BastaService.submitOrder(this.formdata);
                    };
                }


                this.submitEdited = function(){
                    BastaService.submitEditedOrder(this.data.id, this.data.request);
                }


                this.editor = function(){
                    if(this.formvalid){
                        BastaService.editOrder(this.formdata).then(function(response){
                            this.data ={
                                id : response.data.id,
                                request:response.data.externalRequest
                            };
                            console.log(this.data);
                        }.bind(this));
                    };
                }
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "partials/orderform/directives/orderform-submit-buttons.html"
        };
    }]);

