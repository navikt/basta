'use strict';
var util = require('../../utils/util')
module.exports = ['$http', '$location', '$scope','accessChecker', 'notificationService',
		function ( $http, $location, $scope, accessChecker, notificationService){
			this.settings={zone:'fss', environmentClass:'u', application:''}
			
			var crtl=this;
			
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
	         
	         this.changeApplicationMapping= function(item){
	        	 console.log("change app " + item + " " + this.settings.application2)
	         }
	         
	         this.changeZone = function (zone) {
	            this.settings.zone = zone;
		     }
	         
	         getApplications().success(function(data){
	        	 crtl.choices.applications = toArray(data.collection.application);
	         });
	         
	         function getApplications() {
	             return $http({method: 'GET', url: 'api/helper/fasit/applications', transformResponse: util.xmlTojson}).error(
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
	        	 $http.post('rest/orders/serviceuser',_.omit(this.settings))
                 	.success(onOrderSuccess).error(onOrderError);
	         };
	         
	         function onOrderSuccess(order) {
	        	 	console.log("received order " + order);
	                $location.path('/order_details/' + order.id);
	            }

	            function onOrderError(data, status, headers, config) {
	                errorHandler('Ordreinnsending', 'orderSend')(data, status, headers, config);
	            }

		}];