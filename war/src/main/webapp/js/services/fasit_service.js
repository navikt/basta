'use strict';

angular.module('basta.fasit_service', []).service('FasitService', ['$http', '$q', 'errorService', function($http,$q, errorService){


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
        return $http({method: 'GET', url: 'api/helper/fasit/applications', transformResponse: xml2json})
            .error(errorService.handleHttpError('Applikasjonsliste', 'applicationMapping'))
            .then(function onSuccess(response){
                return _.map(toArray(response.data.collection.application), mapAppInfo);
        });
    };

    this.applications = function(){
            return $http({method: 'GET', url: 'api/helper/fasit/applications', transformResponse: xml2json})
                .error(errorService.handleHttpError('Applikasjonsliste', 'applicationMapping'));
    };

    this.applicationGroups = function(){
            return $http({method: 'GET', url: 'api/helper/fasit/applicationGroups'}).error(
                errorService.handleHttpError('Applikasjonsgruppeliste', 'applicationMapping')
            );
    };

    this.applicationAndApplicationGroups = $q.all([this.applications(), this.applicationGroups()]).then(function onSuccess(data) {


            var applications = toArray(data[0].data.collection.application);
            var selectableApps = _.chain(applications)
                .filter(function (application) {return application.applicationGroup === undefined;})
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

    return {
        applications : this.applicationsOnly(),
        all          : this.applicationAndApplicationGroups
    };


}]);

