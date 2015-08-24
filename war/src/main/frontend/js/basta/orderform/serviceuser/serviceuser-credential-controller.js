'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

    this.settings = {
	environmentClass : 'u',
	zone : 'fss',
	application : undefined
    }

    var updateChoices = function(data) {
	this.choices = data;
    };

    FasitService.applications.then(updateChoices.bind(this))

    this.isInFasit = false;
    this.isInAD = false;
    var ctrl = this;

    this.changeEnvironmentClass = function() {
	checkIfExist(this.settings);
    }

    this.changeApplication = function() {
	checkIfExist(this.settings);
    }
    
    this.changeZone = function() {
	checkIfExist(this.settings);
    }

    function checkIfExist(settings) {
	if (!_.isEmpty(settings.application)) {
	    console.log("Checking if user exist ", settings)
	    checkIfResourceExistInFasit(settings);
	    checkIfUserIsInAD(settings);
	}
    }

    function checkIfResourceExistInFasit(settings) {
	$http.get('rest/orders/serviceuser/credential/existInFasit', {
	    params : _.omit(settings)
	}).error(errorService.handleHttpError('Fasit sjekk om ressurs eksisterer')).success(function(data) {
	    // console.log("finnes i fasit", data);
	    ctrl.isInFasit = data;
	});
    }
    ;
    function checkIfUserIsInAD(settings) {
	$http.get('rest/orders/serviceuser/credential/existInAD', {
	    params : _.omit(settings)
	}).error(errorService.handleHttpError('Sjekk om bruker eksisterer i AD')).success(function(data) {
//	    console.log("finnes i AD", data);
	    ctrl.isInAD = data;
	});
    }
    ;

    this.submitOrder = function() {
	console.log("Posting credential order with data", this.settings)
	BastaService.submitOrderWithUrl('rest/orders/serviceuser/credential', this.settings);
    };

} ];
