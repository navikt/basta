'use strict';

module.exports = [ '$q', '$http', function($q, $http) {


	// Trick to always get an array. Xml2json will make one item arrays into an
	// object
	function toArray(obj) {
		return [].concat(obj);
	}

	function createQueueManagerObject(item) {
		// console.log(item)
		var obj = {
			'alias' : item.alias,
			'environmentClass' : item.environmentClass,
			'environmentName' : item.environmentName,
			'name' : item.properties.filter(function(i) {
				return i.name === 'name';
			})[0].value,
			'hostname' : item.properties.filter(function(i) {
				return i.name === 'hostname';
			})[0].value,
			'port' : item.properties.filter(function(i) {
				return i.name === 'port';
			})[0].value,
			'usedby' : _.map(item.usedInApplication, function(app) {
				return app.name
			}),
		};
		obj['url'] = 'mq://' + obj.hostname + ":" + obj.port + "/" + obj.name;

		return obj;
	}

	function extractAliases(values) {
		return _.chain(toArray(values)).pluck('alias').uniq().value();
	}

	function extractQueueManangerList(fasitList) {
		return _.chain(toArray(fasitList)).map(createQueueManagerObject).groupBy('url').map(function(group) {
			var item = _.first(group)
			item.alias = extractAliases(group)
			return item;
		}).value();
	}
	

	this.getQueueManagers= function(environmentClass, environmentName, application) {
		var useBestMatch= angular.isDefined(environmentName) && angular.isDefined(application);;
		var fasitLookup = $http({
			method : 'GET',
			url : 'rest/v1/fasit/resources',
			params : {
				type : "QueueManager",
				bestmatch : useBestMatch, 
				envClass : environmentClass,
				environment : environmentName,
				application : application
			},
			cache : true
		});
		return fasitLookup.then(function onSuccess(response) {
			return extractQueueManangerList(response.data);
		});
	}

	function extractUsedBy(fasitResources) {
		return _.chain(toArray(fasitResources)).map(function(item) {
			return _.pluck(item.usedInApplication, 'name');
		}).flatten().uniq().value();
	}

	this.createAliasApplicationMap=function() {
		var u = $http({
			method : 'GET',
			url : 'rest/v1/fasit/resources?type=QueueManager&envClass=u&usage=true',
			cache : true
		});
		var t = $http({
			method : 'GET',
			url : 'rest/v1/fasit/resources?type=QueueManager&envClass=t&usage=true',
			cache : true
		});
		var q = $http({
			method : 'GET',
			url : 'rest/v1/fasit/resources?type=QueueManager&envClass=q&usage=true',
			cache : true
		});
		var p = $http({
			method : 'GET',
			url : 'rest/v1/fasit/resources?type=QueueManager&envClass=p&usage=true',
			cache : true
		});
		return $q.all([ u, t, q, p ]).then(function onSuccess(response) {
			var allQms = [].concat(response[0].data, response[1].data, response[2].data, response[3].data);
			return _.chain(allQms).groupBy(function(qm) {
				return qm.alias;
			}).map(function(values, key) {
				// console.log(key, values);
				var obj = {
					alias : key,
					usedby : extractUsedBy(values)
				}
				return obj;
			}).value();

		});
	}
}];
