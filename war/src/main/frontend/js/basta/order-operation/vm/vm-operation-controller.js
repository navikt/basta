'use strict';

var angular = require('angular');

module.exports = [ '$routeParams', "$http", 'BastaService', 'User', function($routeParams, $http, BastaService, User) {

	this.choices = [];

	this.data = {
		host : {}
	}
	this.inSuperUserMode = false;
	
	var vm = this;
	
	init($routeParams.hostname);

	// Initial setup
	function init(hostname) {
		if (hostname) {
			vm.data.host = {
				hostname : hostname
			};
			var params = {
				hostname : hostname
			};
			return $http.get('http://sera.adeo.no/api/v1/servers', {
				params : params
			}).then(function(response) {
				console.log("initial lookup", hostname, response.data);
				vm.choices = response.data;
				if (vm.choices.length === 1) {
					vm.data.host = vm.choices[0];
				}
			});
		}
		
		User.onchange(function() {
			vm.superuser = User.isSuperuser();
		});
	}

	

	this.toogleSuperuser = function() {
		this.inSuperUserMode = !this.inSuperUserMode;
	}

	this.searchHosts = function(hostname) {

		if (hostname && hostname.length > 2) {
			var params = {
				hostname : hostname
			};
			return $http.get('http://sera.adeo.no/api/v1/servers', {
				params : params
			}).then(function(response) {
				console.log("search", hostname, response.data);
				vm.choices = response.data
			});

		}
	};

	this.submitOrder = function() {
		console.log("creating new order", this.data);
		BastaService.submitOrderWithUrl('rest/vm/operations', this.data);
	};
} ];
