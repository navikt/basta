'use strict';

module.exports = ['$http', '$location', 'errorService','FasitService',  function ( $http, $location,  errorService, FasitService ){
	     
		this.settings={zone:'fss', environmentClass:'u', application:''}
		
		 var updateChoices = function (data) {
			 this.choices = data;
	     };
	     
	    FasitService.applications.then(updateChoices.bind(this))
		
		this.isInFasit=false;
	    var ctrl=this;
		
		this.changeEnvironmentClass = function () {
            if (this.settings.environmentClass === 'u') {
                this.settings.zone = 'fss';
            }
         }
		 
         this.changeApplication= function(){
	        	//console.log("changed application to "+ this.settings.application)
	        	checkIfResourceExistInFasit(this.settings);
	        }
	        	
               
        function checkIfResourceExistInFasit(settings){
	        	 $http.get('rest/orders/serviceuser/Credential/resourceExists',{ params: _.omit(settings)})
	        	.error(errorService.handleHttpError('Fasit sjekk om servicebruker eksisterer'))
	        	.success(function(data){
		            ctrl.isInFasit =  data; 
		        });
	         };

         this.submitOrder= function(valid){
        	 if(valid){
	        	 $http.post('rest/orders/serviceuser/credential',_.omit(this.settings))
	             	.success(onOrderSuccess)
	             	.error(errorService.handleHttpError('Bestilling'));
	         }
         };
         
         function onOrderSuccess(order) {
        	 	console.log("received order " + order);
                $location.path('/order_details/' + order.id);
            }

}];