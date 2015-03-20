'use strict';

angular.module('basta.serviceuser.certificate.order_form_controller', [])
	.controller('ServiceUserCertificateFormController', 
		['$http', '$location', 'errorService',
		function ( $http, $location, errorService){

			this.settings={zone:'fss', environmentClass:'u', application:''}
			var ctrl=this;
			this.isInFasit=false;
			
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
			            console.log("From fasit " + data);
			            ctrl.isInFasit =  data; 
			        });
		         };

	         this.submitOrder= function(){
	        	 $http.post('rest/orders/serviceuser/certificate',_.omit(this.settings))
                 	.success(onOrderSuccess)
                 	.error(errorService.handleHttpError('Bestilling'));
	         };
	         
	         function onOrderSuccess(order) {
	        	 	console.log("received order " + order);
	                $location.path('/order_details/' + order.id);
	            }


		}]);
