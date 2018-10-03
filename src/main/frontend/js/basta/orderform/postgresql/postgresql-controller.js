'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$http', '$routeParams', function (BastaService, $http, $routeParams) {

    this.data = {
        nodeType: 'DB_POSTGRESQL',
        environmentClass: $routeParams.environmentClass || 'u',
        environmentName: $routeParams.environmentName || null,
        zone: $routeParams.zone || 'fss',
        applicationName: $routeParams.applicationName || null,
        databaseName: $routeParams.databaseName || null,
        fasitAlias: $routeParams.fasitAlias || null
    };

    this.changeEnvironmentClass = function () {
        this.data.environmentName = null;
    };

    this.changeZone = function () {
    };

    this.updateDbAliasSuggestion = function () {
        this.data.fasitAlias = this.data.applicationName + "DB"
    };

    this.updateDbNameSuggestion = function () {
        if (this.data.environmentName && this.data.applicationName) {
            var dbName = this.data.applicationName + "_" + this.data.environmentName;
            this.data.databaseName = trimToLength(removeIllegalCharacters(dbName.toUpperCase()), 28)
        }
    };

    this.updateSuggestions = function () {
        this.updateDbAliasSuggestion();
        this.updateDbNameSuggestion()
    };

    var trimToLength = function (string, length) {
        if (string.length <= length) {
            return string
        } else {
            return string.slice(0, length)
        }
    };

    var removeIllegalCharacters = function (string) {
        return string.replace(/[^A-Za-z0-9_]/g, "")
    };

    this.submitOrder = function () {
        BastaService.submitOrderWithUrl('rest/v1/postgresql', this.data);
    };

}];
