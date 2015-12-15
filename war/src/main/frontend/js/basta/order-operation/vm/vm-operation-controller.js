'use strict';

var angular = require('angular');

module.exports = [ '$routeParams', "$http", 'BastaService', 'User', 'errorService', function($routeParams, $http, BastaService, User, errorService) {

	this.choices = [];

	this.data = {
		hostname : undefined,
		host : undefined,
	}
	this.inSuperUserMode = false;
	this.hasAccess=false;

	var vm = this;

	init($routeParams.hostname);

	// Initial setup
	function init(hostname) {
		if (hostname) {
			vm.data.hostname = hostname;
			return lookupSera(hostname, function(response) {
//				console.log("initial lookup", hostname, response.data);
				vm.choices = _.map(response.data, function(host) {
					return host.hostname
				});
				if (response.data.length === 1) {
					setSelected(response.data[0]);
				}
			});
		}
		
		User.onchange(function() {
			vm.superuser = User.isSuperuser();
		});
	}

	function setSelected(host){
		vm.data.host = host;
		vm.hasAccess = User.hasEnvironmentClassAccess(host.environmentClass);
	}
	
	function lookupSera(hostname, onSuccess) {
		var params = {
			hostname : hostname
		};
		return $http.get('http://sera.adeo.no/api/v1/servers', {
			params : params
		}).then(onSuccess, errorService.handleHttpError('Sera lookup'));
	}


	this.toogleSuperuser = function() {
		this.inSuperUserMode = !this.inSuperUserMode;
	}

	this.onSelectHost = function() {
		return lookupSera(vm.data.hostname,function(response) {
//			console.log("selected", vm.data.hostname, response.data);
			if (response.data.length === 1) {
				setSelected(response.data[0]);
			}
		});
	}

	this.searchHosts = function(hostname) {
		if (hostname && hostname.length > 3) {
		return lookupSera(hostname, function(response) {
//				 console.log("search", hostname, response.data);
				vm.choices = _.map(response.data, function(host) {
					return host.hostname
				})
			});

		}
	};

	this.start = function() {
		var hostnames = [vm.data.host.hostname];
		console.log("starting", hostnames);
		BastaService.createOrderNoFlatmap('rest/vm/operations/start', hostnames);
	};

	this.stop = function() {
		var hostnames = [vm.data.host.hostname];
		console.log("stopping", hostnames);
		BastaService.createOrderNoFlatmap('rest/vm/operations/stop', hostnames);
	};

	this.remove = function() {
		var hostnames = [vm.data.host.hostname];
		console.log("deleting", hostnames);
		BastaService.createOrderNoFlatmap('rest/vm/operations/decommission', hostnames);
	};
} ];
