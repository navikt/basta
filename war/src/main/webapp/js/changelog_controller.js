'use strict';

angular.module('basta.changelog_controller', [])
    .controller('changelogController', ['$scope', '$rootScope', '$http', '$routeParams', '$location', '$resource', function ($scope, $rootScope, $http, $routeParams, $location, $resource) {

        $scope.version = {}
        $scope.versions = [];
        $scope.currentVersion = {};
        $scope.nextVersion;
        $scope.previousVersion;
        $scope.firstTime;

        $http.get("rest/jiraproxy", {params: {"path": "rest/api/2/project/AURA/versions"}}).success(function (versionsArray) {
            var releasedBastaVersionsFilter = function (version) {
                return _(version.name).startsWith("basta:") && version.released == true;
            };

            var enrichWithVersionName = function (version) {
                version.versionName = version.name.split(":")[1];
                return version;
            };

            $scope.versions = _.map(_.filter(versionsArray, releasedBastaVersionsFilter), enrichWithVersionName);
            $scope.currentVersion = getVersion($scope.version);

            setPreviousAndNextVersions();
        });

        var getVersion = function (version) {
            return _.find($scope.versions, function (elem) {
                return elem.versionName == version;
            });
        }

        function setPreviousAndNextVersions() {
            var currentVersionIndex = _.indexOf($scope.versions, $scope.currentVersion);
            var previousVersion = $scope.versions[currentVersionIndex - 1];
            if (!_.isUndefined(previousVersion)) {
                $scope.previousVersion = previousVersion.versionName;
            }
            var nextVersion = $scope.versions[currentVersionIndex + 1];
            if (!_.isUndefined(nextVersion)) {
                $scope.nextVersion = nextVersion.versionName;
            }
        }

        if (!_.isUndefined($routeParams.version)) {
            $scope.version = $routeParams.version;
            if (!_.isUndefined($routeParams.firstTime)) {
                $scope.firstTime = $routeParams.firstTime;
                console.log("set firstTime to " + $scope.firstTime);
            }
        } else {
            $location.url("/");
        }

        $http.get("rest/jiraproxy", {params: {"path": 'rest/api/2/search?jql=project+%3D+\"AURA\"+AND+fixVersion+%3D+\"basta%3A' + $scope.version + '\"&tempMax=1000'}}).success(function (issuesArray) {
            $scope.issues = issuesArray.issues;
        });

        $scope.isCurrentlySelected = function (version) {
            return  version == $scope.version;
        }

    }]);

