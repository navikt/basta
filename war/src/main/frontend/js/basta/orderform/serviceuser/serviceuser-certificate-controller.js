'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

    this.settings = {
	environmentClass : 'u',
	application : undefined
    }

    var updateChoices = function(data) {
	this.choices = data;
    };

    FasitService.applications.then(updateChoices.bind(this))

    this.isInFasit = false;
    var ctrl = this;

    this.changeEnvironmentClass = function() {
	checkIfExist(this.settings);
    }

    this.changeApplication = function() {
	checkIfExist(this.settings);
    }

    function checkIfExist(settings) {
	if (!_.isEmpty(settings.application)) {
	    console.log("Checking if user exist ", settings)
	    checkIfResourceExistInFasit(settings);
	}
    }

    function checkIfResourceExistInFasit(settings) {
	$http.get('rest/orders/serviceuser/certificate/existInFasit', {
	    params : _.omit(settings)
	}).error(errorService.handleHttpError('Fasit sjekk om sertifikat eksisterer')).success(function(data) {
	    // console.log("finnes i fasit", data);
	    ctrl.isInFasit = data;
	});
    };
    

    this.submitOrder = function() {
	console.log("Posting certificate order with data", this.settings)
	BastaService.submitOrderWithUrl('rest/orders/serviceuser/certificate', this.settings);
    };

} ];
