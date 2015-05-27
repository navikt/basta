'use strict';

module.exports = ['$http', 'errorService', 'FasitService', 'BastaService', function ( $http, errorService,  FasitService, BastaService ){
	
	
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
	        console.log("changed application to "+ this.settings.application)
	        	checkIfResourceExistInFasit(this.settings);
	        }
	        	
               
        function checkIfResourceExistInFasit(settings){
	        	 $http.get('rest/orders/serviceuser/Certificate/resourceExists',{ params: _.omit(settings)})
	        	.error(errorService.handleHttpError('Fasit sjekk om sertifikat eksisterer'))
	        	.success(function(data){
//	        		console.log("finnes i fasit", data);
		            ctrl.isInFasit =  data; 
		        });
	         };

         this.submitOrder= function(){
        	 console.log("Posting certificate order with data", this.settings )
        	BastaService.submitOrderWithUrl('rest/orders/serviceuser/certificate', this.settings);
         };
         
       

}];
