'use strict';

angular.module('skyBestApp.fasit_resource', [])
  .directive('fasitResource', ['$http', 'errorService', function($http, errorService) {
    
  return {
    restrict: 'E',
    templateUrl: 'partials/resource_directive.html',
    require: 'ngModel',
    scope: {
      model: '=ngModel',
      fieldError: '=',
      environmentClass: '=',
      environmentName: '=',
      zone: '=',
      applicationName: '=',
      resourceType: '@',
      title: '@',
      fieldId: '@',
      choices: '=?'
    },
    link: function(scope, element, attrs) {
      scope.busy = false;
      function withDomain(f) {
        return $http({ method: 'GET', url: 'rest/domains', params: {envClass: scope.environmentClass, zone: scope.zone}})
          .success(f).error(errorService.handleHttpError('Domener'));
      }
      function reevaluate() {
        if (scope.environmentName && scope.environmentClass && scope.zone) {
          scope.busy = true;
          withDomain(function(domain) {
            var query = {
                bestmatch: false,
                domain: domain,
                envClass: scope.environmentClass, 
                envName: scope.environmentName, 
                app: scope.applicationName,
                type: scope.resourceType
            };
            $http({ method: 'GET', url: 'api/helper/fasit/resources', params: query, transformResponse: xml2json })
              .success(function(data) {
                scope.busy = false;
                if (!_.isUndefined(data.collection.resource)){
                    scope.choices = _.chain(data.collection.resource).arrayify().pluck('alias').value();
                }
              }).error(errorService.handleHttpError(scope.title));
          });
        }
      }
      scope.$watch('environmentName', reevaluate);
      scope.$watch('environmentClass', reevaluate);
      scope.$watch('zone', reevaluate);
    }
  };
}]);
