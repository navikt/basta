'use strict';

var angular = require('angular');

module.exports = [ '$routeParams', "$http", 'BastaService', 'User', 'errorService', function($routeParams, $http, BastaService, User, errorService) {

	var Map = require("collections/map");
	this.results = new Map(); 
	
	this.data = {
		hostname : $routeParams.hostname,
		
	}
	this.hasAccess = true;

	var vm = this;

	init($routeParams.hostname);

	// Initial setup
	function init() {
		if (vm.data.hostname) {
			console.log("initial lookup ", vm.data.hostname);
			return findHostnames(vm.data.hostname);
		}

		User.onchange(function() {
			vm.superuser = User.isSuperuser();
		});
	}

	function findHostnames(hostnames) {
		if (!hostnames){
			return;
		}

		var hostnameArray = hostnames.split(",").map(function(s){
			return s.trim();
		});
		// remove not existing in searchstring
		vm.results.keys().forEach(function(key){
			if(hostnameArray.indexOf(key) === -1){
				console.log("key is not in searchstring", key);
				vm.results.delete(key);
			}
		});
		
		// add new elements
		hostnameArray.forEach(function(hostname) {
//			console.log("new host", hostname);
			if (!vm.results.has(hostname) && hostname.length > 3) {
				lookupSera(hostname);
			}
		});
	}

	function lookupSera(hostname) {
		var params = {
			hostname : hostname
		};
		return $http.get('http://sera.adeo.no/api/v1/servers', {
			params : params
		}).then(function(response) {
//			console.log("lookup", vm.data.hostname, response.data);
			if (response.data.length === 1) {
				vm.results.set(hostname, response.data[0]);
			}
		}, errorService.handleHttpError('Sera lookup'));
	}

	this.hostnameChanged = function() {
		return findHostnames(vm.data.hostname);
	}
	
	function extractHostnames(){
		return vm.results.map(function(host){
			return host.hostname
		})
	}


	this.start = function() {
		var hostnames = extractHostnames();
		console.log("starting", hostnames);
		BastaService.createOrderNoFlatmap('rest/vm/operations/start', hostnames);
	};

	this.stop = function() {
		var hostnames = extractHostnames();
		console.log("stopping", hostnames);
		BastaService.createOrderNoFlatmap('rest/vm/operations/stop', hostnames);
	};

	this.remove = function() {
		var hostnames = extractHostnames();
		console.log("deleting", hostnames);
		BastaService.createOrderNoFlatmap('rest/vm/operations/decommission', hostnames);
	};
} ];
