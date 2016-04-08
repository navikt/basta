'use strict';

var angular = require('angular');
var Map = require("collections/map");

module.exports = [ '$routeParams', "$http", 'BastaService', 'User', 'errorService', function($routeParams, $http, BastaService, User, errorService) {
	
	this.results = new Map(); 

	this.data = {
		hostname : $routeParams.hostname,
	}
	
	var vm = this;

	init($routeParams.hostname);

	// Initial setup
	function init() {
		if (vm.data.hostname) {
			console.log("initial lookup ", vm.data.hostname);
			return findHostnames(vm.data.hostname);
		}
		
	}

	function findHostnames(hostnames) {
		if (!hostnames){
			return;
		}

		var hostnameArray = splitAndTrim(hostnames);
		// remove not existing in searchstring
		vm.results.keys().forEach(function(key){
			if(hostnameArray.indexOf(key) === -1){
//				console.log("key is not in searchstring", key);
				vm.results.delete(key);
			}
		});
		
		// add new elements
		hostnameArray.forEach(function(hostname) {
//          console.log("new host", hostname);
			if (!vm.results.has(hostname) && hostname.length > 3) {
				lookupSera(hostname);
			}
		});
	}
	
	function splitAndTrim(hostnames){
		if(!hostnames){
			return [];
		}
		return	hostnames.split(",").map(function(s){
			return s.trim();
		});
	}

	function lookupSera(hostname) {
		var params = {
			hostname : hostname
		};
		return $http.get('https://sera.adeo.no/api/v1/servers', {
			params : params
		}).then(function(response) {
//         console.log("lookup", vm.data.hostname, response.data);
			if (response.data.length === 1) {
				var server= response.data[0];
				server.hasAccess=hasAccessToHost(server.hostname);
				vm.results.set(hostname, server);
				
			}
		}, errorService.handleHttpError('Sera lookup'));
	}
	
	function hasAccessToHost(hostname){
		if(!hostname && hostname.length < 2){
			return false;
		} 
		switch (hostname.toLowerCase().charAt(0)){
		case 'a':
			return User.hasEnvironmentClassAccess('p');
		case 'b':
			return User.hasEnvironmentClassAccess('q');
		case 'd':
			return User.hasEnvironmentClassAccess('t');
		case 'e':
			return User.hasEnvironmentClassAccess('u');
		default:
			console.log("Unknown servername prefix " + hostname + " Assuming p access" );
			return User.hasEnvironmentClassAccess('p');
		}
		
	}
	
	
	function extractHostnames(){
		if (vm.results && vm.results.length > 0){
			return vm.results.map(function(host){
				return host.hostname
			})
		} else{
			return splitAndTrim(vm.data.hostname);
		}
	}

	this.hostnameChanged = function() {
		return findHostnames(vm.data.hostname);
	}
	
	this.hasAccess = function(){
		var hostnames = extractHostnames();
		var access= hostnames.length > 0;
		hostnames.forEach(function(hostname){
				access= access && hasAccessToHost(hostname)
			});
		return access;
	}
	
	this.getHostNames = function(){
		return extractHostnames().join(",");
	}
	
	this.start = function() {
		var hostnames = extractHostnames();
		console.log("starting", hostnames);
		BastaService.postOrder('rest/vm/operations/start', hostnames);
	};

	this.stop = function() {
		var hostnames = extractHostnames();
		console.log("stopping", hostnames);
		BastaService.postOrder('rest/vm/operations/stop', hostnames);
	};

	this.remove = function() {
		var hostnames = extractHostnames();
		console.log("deleting", hostnames);
		BastaService.postOrder('rest/vm/operations/decommission', hostnames);
	};
} ];
