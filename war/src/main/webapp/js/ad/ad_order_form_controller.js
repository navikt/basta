'use strict';

angular.module('basta.ad.order_form_controller', []).controller('adOrderFormController', 
		['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', '$q', 'accessChecker', 'notificationService',
		function ($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache, $q, accessChecker, notificationService){
			this.settings={zone:'fss', environmentClass:'u', application:''}
			

			this.choices = {
				zones : [ 'fss', 'sbs' ],
				environmentClasses : [ 'u', 't', 'q', 'p' ],
				environmentClassNames : {
					u : 'Utvikling',
					t : 'Test',
					q : 'PreProd',
					p : 'Produksjon'
				},
				applications:{}
				
			};
			
			
			
			 this.changeEnvironmentClass = function (environmentClass) {
                this.settings.environmentClass = environmentClass;

                if (this.settings.environmentClass === 'u') {
                    this.settings.zone = 'fss';
                }
	         }
			 
			 $scope.hasEnvironmentClassAccess = function (environmentClass) {
	            return accessChecker.hasEnvironmentClassAccess($scope, environmentClass);
	         };
	         
	         this.changeZone = function (zone) {
	            this.settings.zone = zone;
		     }
	         
	         getApplications().success(function(data){
	        	 $scope.applications = toArray(data.collection.application);
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
	         };



		}]);
