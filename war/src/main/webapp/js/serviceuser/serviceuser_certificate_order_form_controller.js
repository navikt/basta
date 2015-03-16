'use strict';

angular.module('basta.serviceuser.certificate.order_form_controller', [])
	.controller('ServiceUserCertificateFormController', 
		['$http', '$location', '$scope','accessChecker', 'notificationService',
		function ( $http, $location, $scope, accessChecker, notificationService){
			this.settings={zone:'fss', environmentClass:'u', application:''}
			
			var ctrl=this;
			
			this.choices = {
				zones : [ 'fss', 'sbs' ],
				environmentClasses : [ 'u', 't', 'q', 'p' ],
				environmentClassNames : {
					u : 'Utvikling',
					t : 'Test',
					q : 'PreProd',
					p : 'Produksjon'
				},
				applications:[]
				
			};
			
			
			
			 this.changeEnvironmentClass = function (environmentClass) {
                this.settings.environmentClass = environmentClass;

                if (this.settings.environmentClass === 'u') {
                    this.settings.zone = 'fss';
                }
	         }
			 
			 this.hasEnvironmentClassAccess = function (environmentClass) {
	            return accessChecker.hasEnvironmentClassAccess($scope, environmentClass);
	         };
	         
	         this.changeZone = function (zone) {
	            this.settings.zone = zone;
		     }
	         
	         getApplications().success(function(data){
	        	 ctrl.choices.applications = toArray(data.collection.application);
	         });
	         
	         function getApplications() {
	             return $http({method: 'GET', url: 'api/helper/fasit/applications', transformResponse: xml2json}).error(
	                   errorHandler('Applikasjonsliste', 'applicationMapping')
	             );
	         }
	      
	         function errorHandler(name, busyIndicator) {
	                return function (data, status, headers, config) {
	                    if (busyIndicator)
	                        delete $scope.busies[busyIndicator];
	                    $rootScope.$broadcast('GeneralError', {
	                        name: name,
	                        httpError: {data: data, status: status, headers: headers, config: config}
	                    });
	                };
	            }
	         
	         // Trick to always get an array. Xml2json will make one item arrays into an object
	            function toArray(obj) {
	                return [].concat(obj);
	            }
	            
	            
	         this.submitOrder= function(){
	        	 console.log(this.settings);
	        	 $http.post('rest/orders/serviceusercertificate',_.omit(this.settings))
                 	.success(onOrderSuccess).error(onOrderError);
	         };
	         
	         function onOrderSuccess(order) {
	        	 	console.log("received order " + order);
	                $location.path('/order_details/' + order.id);
	            }

	        function onOrderError(data, status, headers, config) {
	             errorHandler('Ordreinnsending', 'orderSend')(data, status, headers, config);
	         }
	            
	         this.formCompleted= function(){
	        	return this.settings.application != '';
	         };
	         
	        
	         this.changeApplication= function(){
	        	console.log("changed to "+ this.settings.application)
	        	checkIfResourceExistInFasit(this.settings);
	        }
	        	
	               
	        function checkIfResourceExistInFasit(settings){
		        	 $http.get('rest/orders/serviceusercertificate/resourceExists',{ params: _.omit(settings)})
		        	.error(errorHandler('Fasit', 'Resource'))
		        	.success(function(data){
			            console.log("From fasit " + data);
			            ctrl.fasitAnswer =  data; 
			        });
		         };
		     

		}]);
