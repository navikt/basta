'use strict';

var util = require('../utils/util');
module.exports = ['$http', '$q', 'errorService', function ($http, $q, errorService) {


    // Trick to always get an array. Xml2json will make one item arrays into an object
    function toArray(obj) {
        return [].concat(obj);
    }

    // Used for build json object for both applications and applicationgroups.
    // When we have an application group, the property applications will be added
    // and will contain a list of applications in the applicationgroup
    var mapAppInfo = function (item) {
//    	 console.log(item);
        var obj = {"name": item.name};
        if (item.applications) {
            var value = _.pluck(toArray(item.applications), "name");
            if (!_.isBlank(value)) {
                obj["applications"] = value;
            }
        }
        return obj;
    }

    this.applicationsOnly = function () {
        return $http({method: 'GET', url: 'rest/v1/fasit/applications'})
            .error(errorService.handleHttpError('Applikasjonsliste', 'applicationMapping'))
            .then(function onSuccess(response) {
                return _.chain(toArray(response.data))
                    .map(mapAppInfo)
                    .sortBy(function (app) {
                        return app.name.toLowerCase()
                    })
                    .value();
            });
    };


    this.environments = function () {
        return $http({method: 'GET', url: 'rest/v1/fasit/environments'})
            .error(errorService.handleHttpError('Miljøliste', 'environmentName'))
            .then(function onSuccess(response) {
                return _.chain(toArray(response.data))
                    .groupBy('envClass')
                    .map(function (e, k) {
                        return [k, _.chain(e)
                            .map(function (e) {
                                return {
                                    name: e.name
                                };
                            })
                            .sortBy('name')
                            .value()]
                            ;
                    })
                    .object()
                    .value();
            });
    };


    this.applications = function () {
        return $http({method: 'GET', url: 'rest/v1/fasit/applications'})
            .error(errorService.handleHttpError('Applikasjonsliste', 'applicationMapping'));
    };

    this.clusters = function (environment) {
        return $http({method: 'GET', url: 'rest/v1/fasit/clusters', params: {environment: environment}})
            .error(errorService.handleHttpError('Clusterliste', 'clusterNames'));
    };

    this.applicationGroups = function () {
        return $http({method: 'GET', url: 'rest/v1/fasit/applicationgroups'}).error(
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
                .filter(function (appGrp) {
                    return appGrp.applications !== undefined;
                })
                .map(mapAppInfo)
                .value();


        var result = _.chain(selectableApps.concat(selectableAppGrps))
                .sortBy(function (obj) {
                    return obj.name.toLowerCase()
                })
                .value();

        return result;
        }
    );

    this.isMultiSite = function (envClass, envName) {
        if (envClass === 'p') {
            return true;
        }
        return _(['q0', 'q1', 'q3']).contains(envName);
    }

    return {
        applications: this.applicationsOnly(),
        all: this.applicationAndApplicationGroups,
        environments: this.environments(),
        clusters: this.clusters,
        isMultiSite: this.isMultiSite,
        queueManagers: this.queueManagers
    };

}];

