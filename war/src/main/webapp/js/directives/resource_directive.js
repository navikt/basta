'use strict';

angular.module('skyBestApp.fasit_resource', [])
    .directive('fasitResource', ['$http', 'errorService', function ($http, errorService) {

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
                choices: '=?',
                defaultValue: '@'
            },
            link: function (scope, element, attrs) {

                scope.defaultMatch = defaultValueMatch();
                scope.show = (typeof(scope.model) !== 'undefined');
                scope.busy = false;
                function withDomain(f) {
                    return $http({ method: 'GET', url: 'rest/vm/domains', params: {envClass: scope.environmentClass, zone: scope.zone}})
                        .success(f).error(errorService.handleHttpError('Domener'));
                }

                function defaultValueMatch() {
                    if (scope.defaultValue) {
                        var candidate = _.chain(scope.choices)
                            .filter(function (e) {
                                return (_.isEqual(e, scope.defaultValue))
                            })
                            .value();
                        return (_.size(candidate) === 1);
                    }
                }

                function reevaluate(newVal, oldVal) {

                    if (oldVal === "" && newVal === 'undefined')
                        scope.choices = []; // Always clear scope when changes to env, envClass or zone are made
                    if (scope.environmentName && scope.environmentClass && scope.zone) {
                        scope.busy = true;
                        scope.show = (typeof(scope.model) !== 'undefined');

                        if (scope.show && scope.defaultValue) {
                            console.log("resetting model");
                            scope.model = "";
                        }

                        withDomain(function (domain) {
                            var query = {
                                bestmatch: false,
                                domain: domain,
                                envClass: scope.environmentClass,
                                envName: scope.environmentName,
                                app: scope.applicationName,
                                type: scope.resourceType
                            };

                            $http({ method: 'GET', url: 'api/helper/fasit/resources', params: query, transformResponse: xml2json })
                                .success(function (data) {
                                    scope.busy = false;

                                    if (!_.isUndefined(data.collection.resource)) {
                                        scope.choices = _.chain(data.collection.resource).arrayify().pluck('alias').value();
                                        if (scope.show && defaultValueMatch()) {
                                            scope.model = scope.defaultValue;
                                        }
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
