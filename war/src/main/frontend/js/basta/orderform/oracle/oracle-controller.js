'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$http', '$routeParams', function (BastaService, $http, $routeParams) {

    this.data = {
        nodeType: 'DB_ORACLE',
        environmentClass: $routeParams.environmentClass || 'u',
        environmentName: $routeParams.environmentName || null,
        zone: $routeParams.zone || 'fss',
        applicationName: $routeParams.applicationName || null,
        templateURI: $routeParams.templateURI || null,
        fasitAlias: $routeParams.fasitAlias || null
    }
    this.selectedTemplate = null;

    this.updateTemplates = function (environmentClass, zone) {
        $http.get('rest/v1/oracledb/templates', {
            params: {
                environmentClass: environmentClass,
                zone: zone
            }
        }).success(function (data) {
            this.templates = data;
        }.bind(this));
    }

    this.updateTemplates(this.data.environmentClass, this.data.zone)

    this.clearTemplates = function () {
        this.templates = null
        this.selectedTemplate = null
        this.data.templateURI = null
    }

    this.changeEnvironmentClass = function () {
        this.clearTemplates()
        this.data.environmentName = null
        this.updateTemplates(this.data.environmentClass, this.data.zone)
    }

    this.changeZone = function () {
        this.clearTemplates()
        this.updateTemplates(this.data.environmentClass, this.data.zone)
    }

    this.updateDbAliasSuggestion = function() {
        this.data.fasitAlias = this.data.applicationName + "DB"
    }

    this.selectTemplate = function () {
        this.data.templateURI = this.selectedTemplate.uri
    }

    this.submitOrder = function () {
        BastaService.submitOrderWithUrl('rest/v1/oracledb', this.data);
    };

}];
