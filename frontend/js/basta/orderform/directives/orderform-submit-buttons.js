'use strict';

module.exports = ['User','BastaService', function (User, BastaService) {
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


            controller: ['$scope', function ($scope) {
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
                        }.bind(this));
                    };
                }
            }],
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-submit-buttons.html"
        };
    }];

