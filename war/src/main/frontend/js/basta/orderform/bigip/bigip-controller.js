'use strict';

var angular = require('angular');

module.exports = ['BastaService', '$http', function (BastaService, $http) {

    this.data = {
        nodeType: 'DB_ORACLE',
        environmentClass: 'u',
        environmentName: null,
        zone: 'fss',
        applicationName: null,
        templateURI: null,
        fasitAlias: null
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
