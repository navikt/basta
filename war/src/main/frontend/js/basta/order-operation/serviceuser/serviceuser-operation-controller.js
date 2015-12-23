'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService','$routeParams', function($http, errorService, FasitService, BastaService, $routeParams) {

	this.settings = {
		environmentClass : $routeParams.environmentClass || 'u',
		zone : $routeParams.zone || 'fss',
		application : $routeParams.application
	}

	this.data = {
		isInFasit : false,
		isInAD : false,
		user : ''
	}
	var vm = this;

	var updateChoices = function(data) {
		this.choices = data;
	};

	FasitService.applications.then(updateChoices.bind(this))
	
	init();
	
	function init(){
		checkIfExist(vm.settings);
	}	
	
	
	this.changeEnvironmentClass = function() {
		checkIfExist(this.settings);
	}

	this.changeApplication = function() {
		checkIfExist(this.settings);
	}

	this.changeZone = function() {
		checkIfExist(this.settings);
	}

	this.stop = function() {
		console.log("Stopping  ", vm.settings)
		BastaService.submitOrderWithUrl('rest/operation/serviceuser/credential/stop', vm.settings);
	}

	this.start = function() {
		console.log("Starting  ", vm.settings)
		BastaService.submitOrderWithUrl('rest/operation/serviceuser/credential/start', vm.settings);
	}

	this.remove = function() {
		console.log("Deleting  ", vm.settings)
		BastaService.submitOrderWithUrl('rest/operation/serviceuser/credential/delete', vm.settings);
	}

	function checkIfExist(settings) {
		if (!_.isEmpty(settings.application)) {
//			console.log("Checking if user exist ", settings)
			checkIfResourceExistInFasit(settings);
			checkIfUserIsInAD(settings);
			getUserdn(settings)
		}
	}

	function getUserdn(settings) {
		$http.get('rest/operation/serviceuser/credential/user', {
			params : _.omit(settings)
		}).then(function(response) {
			vm.data.user = response.data;
		}, errorService.handleHttpError('Serviceuser oppslag'));
	}

	function checkIfResourceExistInFasit(settings) {
		$http.get('rest/orders/serviceuser/credential/existInFasit', {
			params : _.omit(settings)
		}).then(function(response) {
			vm.data.isInFasit = response.data;
		}, errorService.handleHttpError('Fasit sjekk om ressurs eksisterer'));
	}

	function checkIfUserIsInAD(settings) {
		$http.get('rest/orders/serviceuser/credential/existInAD', {
			params : _.omit(settings)
		}).then(function(response) {
			vm.data.isInAD = response.data;
		}, errorService.handleHttpError('Sjekk om bruker eksisterer i AD'));
	}
	;

} ];
