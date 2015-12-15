'use strict';

var angular = require('angular');

module.exports = [ '$routeParams', "$http", 'BastaService', 'User', function($routeParams, $http, BastaService, User) {

	this.choices = [];

	this.data = {
		hostname : undefined,
		host : undefined,
	}
	this.inSuperUserMode = false;

	var vm = this;

	init($routeParams.hostname);

	// Initial setup
	function init(hostname) {
		if (hostname) {
			vm.data.hostname = hostname;
			var params = {
				hostname : hostname
			};
			return $http.get('http://sera.adeo.no/api/v1/servers', {
				params : params
			}).then(function(response) {
				console.log("initial lookup", hostname, response.data);
				vm.choices = _.map(response.data, function(host) {
					return host.hostname
				});
				if (response.data.length === 1) {
					vm.data.host = response.data[0];
				}
			});
		}

	}

	User.onchange(function() {
		vm.superuser = User.isSuperuser();
	});

	this.toogleSuperuser = function() {
		this.inSuperUserMode = !this.inSuperUserMode;
	}

	this.onSelectHost = function() {
		var params = {
			hostname : vm.data.hostname
		};
		return $http.get('http://sera.adeo.no/api/v1/servers', {
			params : params
		}).then(function(response) {
			console.log("selected", vm.data.hostname, response.data);
			if (response.data.length === 1) {
				vm.data.host = response.data[0];
			}
		});
	}

	this.searchHosts = function(hostname) {

		if (hostname && hostname.length > 2) {
			var params = {
				hostname : hostname
			};
			return $http.get('http://sera.adeo.no/api/v1/servers', {
				params : params
			}).then(function(response) {
				// console.log("search", hostname, response.data);
				vm.choices = _.map(response.data, function(host) {
					return host.hostname
				})
			});

		}
	};

	this.start = function() {
		console.log("starting", this.data);
	};

	this.stop = function() {
		console.log("stop", this.data);
	};

	this.remove = function() {
		console.log("delete", this.data);
	};
} ];
