'use strict';

var util = require('../utils/util');
module.exports = ['$http', '$q', 'errorService', function($http,$q, errorService){


    // Trick to always get an array. Xml2json will make one item arrays into an object
    function toArray(obj) {
        return [].concat(obj);
    }

    // Used for build json object for both applications and applicationgroups.
    // When we have an application group, the property applications will be added
    // and will contain a list of applications in the applicationgroup
    var mapAppInfo = function (item) {
        var obj = {"name": item.name};
        if (item.applications) {
            var value = _.pluck(toArray(item.applications), "name");
            if (!_.isBlank(value)) {
                obj["applications"] = value;
            }
        }
        return obj;
    }

    this.applicationsOnly = function(){
        return $http({method: 'GET', url: 'api/helper/fasit/applications'})
            .error(errorService.handleHttpError('Applikasjonsliste', 'applicationMapping'))
            .then(function onSuccess(response){
                return _.map(toArray(response.data), mapAppInfo);
        });
    };

    this.environments = function(){
    	return $http({method: 'GET', url: 'api/helper/fasit/environments'})
            .error(errorService.handleHttpError('Miljøliste', 'environmentName'))
            .then(function onSuccess(response) {
                return _.chain(toArray(response.data))
                    .groupBy('envClass')
                    .map(function (e, k) {
                        return [k, _.chain(e)
                            .map(function (e) {return {
                                name: e.name
                            };})
                            .sortBy('name')
                            .value()]
                            ;})
                    .object()
                    .value();
        });
    };


    var transformQueueManager = function (item) {
        var obj = {
        	'alias': item.alias,
        	'name': item.properties.filter(function(i) { return i.name === 'name';})[0].value,
        	'hostname': item.properties.filter(function(i) { return i.name === 'hostname';})[0].value, 
        	'port': item.properties.filter(function(i) { return i.name === 'port';})[0].value,
        	'usedby': _.map(item.usedInApplication, function(app){return app.name}),
       	};
        obj['url']= 'mq://'+ obj.hostname + ":" + obj.port +"/"+ obj.name;
        
//        console.log(obj)
        return obj;
    }

    this.queueManagers = function() {
    	var u = $http({method: 'GET', url: 'api/helper/fasit/resources?type=QueueManager&envClass=u&usage=true'});
    	var t = $http({method: 'GET', url: 'api/helper/fasit/resources?type=QueueManager&envClass=t&usage=true'});
    	var q = $http({method: 'GET', url: 'api/helper/fasit/resources?type=QueueManager&envClass=q&usage=true'});
    	var p = $http({method: 'GET', url: 'api/helper/fasit/resources?type=QueueManager&envClass=p&usage=true'});
    	return $q.all([u,t,q,p])
            		.then(function onSuccess(response) { 
            			return {"u": _.map(toArray(response[0].data), transformQueueManager),
            				    "t": _.map(toArray(response[1].data), transformQueueManager),
            				    "q": _.map(toArray(response[2].data), transformQueueManager),
            				    "p": _.map(toArray(response[3].data), transformQueueManager)};
            		});
    }
    
    this.applications = function(){
            return $http({method: 'GET', url: 'api/helper/fasit/applications'})
                .error(errorService.handleHttpError('Applikasjonsliste', 'applicationMapping'));
    };

    this.applicationGroups = function(){
            return $http({method: 'GET', url: 'api/helper/fasit/applicationGroups'}).error(
                errorService.handleHttpError('Applikasjonsgruppeliste', 'applicationMapping')
            );
    };

    this.applicationAndApplicationGroups = $q.all([this.applications(), this.applicationGroups()]).then(function onSuccess(data) {


            var applications = toArray(data[0].data);
            var selectableApps = _.chain(applications)
                .filter(function (application) {
                    return application.applicationGroup === null || application.applicationGroup === undefined;
                })
                .map(mapAppInfo)
                .value();

            var applicationGroups = data[1].data;
            var selectableAppGrps = _.chain(applicationGroups)
                .filter(function (appGrp) {return appGrp.applications !== undefined;})
                .map(mapAppInfo)
                .value();



            var result = _.chain(selectableApps.concat(selectableAppGrps))
                .sortBy(function (obj) {return obj.name.toLowerCase()})
                .value();
            return result;
        }
    );
    
    this.isMultiSite = function(envClass, envName){
//	console.log("multisite", envClass, envName);
	    if(envClass==='p'){
		    return true;
		}
	        return  _(['q0','q1','q3']).contains(envName);
	}





    return {
        applications  : this.applicationsOnly(),
        all           : this.applicationAndApplicationGroups,
        environments  : this.environments(),
        isMultiSite   : this.isMultiSite,
        queueManagers : this.queueManagers
    };


}];

