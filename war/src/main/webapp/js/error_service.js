'use strict';

angular.module('skyBestApp.error_service', [])
  .service('errorService', ['$rootScope', function($rootScope) {
    this.handleHttpError = function(name) {
      return function(data, status, headers, config) {
          $rootScope.$broadcast('GeneralError', { name: name, httpError: { data: data, status: status, headers: headers, config: config }});
        };
      };

  }]);
