'use strict';

module.exports = ['User','BastaService', function (User, BastaService) {
       
        return {
            restrict: 'E',
            scope: {
                data: '=model',
                formvalid: '=',
                onSelect: '&onSelect',
                formdata: '=formdata',
                submitEditedData: '='
            },


            controller:  function () {
        	var vm=this;
        	
        	User.onchange(function(){
        	    vm.superuser=User.isSuperuser();
        	});

                this.busy=false;

                this.submit = function(){
                    if(this.formvalid){
                    	this.busy=true;
                        BastaService.submitOrder(this.formdata);
                    }else{
                    	console.log('form is not valid. Check errors ')	
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
            },
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-submit-buttons.html"
        };
    }];

