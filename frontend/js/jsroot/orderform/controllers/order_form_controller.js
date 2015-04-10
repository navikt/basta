'use strict';

require('../defaults');
var util = require('../../utils/util');

module.exports = ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', '$q', 'accessChecker', 'notificationService',
        function ($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache, $q, accessChecker, notificationService) {

            retrieveUser();

            if (!accessChecker.isLoggedIn($scope.currentUser)) {
                $location.path('/order_list');
            }
            var isBlockingNotification = notificationService.query().$promise.then(function (notes) {
                var anyNotifications = _.any(notes, function (note) {
                    return note.blockOperations === true;
                });
                if (anyNotifications && !$scope.currentUser.superUser) {
                    $location.path('/order_list');
                }

            });

            $scope.setDefaults = setDefaults;
            $scope.$on('UserChanged', retrieveUser);

            $scope.choices = {
                zones: ['fss', 'sbs'],
                environmentClasses: ['u', 't', 'q', 'p'],
                environmentClassNames: {u: 'Utvikling', t: 'Test', q: 'PreProd', p: 'Produksjon'},
                serverCounts: ['1', '2', '4', '8'],
                serverSizes: {s: {name: 'Standard'}, m: {name: 'Medium'}, l: {name: 'Stor'}},
                middleWareTypeMessages: {}
            };

            setDefaults();

            $scope.hasOrderTypeAccess = function (){
                var superuserOrderTypes = ['PLAIN_LINUX', 'OPEN_AM_SERVER', 'OPEN_AM_PROXY'];
                var x =  $scope.currentUser.superUser ? true : !_(superuserOrderTypes).has($routeParams.orderType);
                console.log(x);
                return x;
            };

            $scope.hasEnvironmentClassAccess = function (environmentClass) {
                return accessChecker.hasEnvironmentClassAccess($scope, environmentClass);
            };

            function useSettingsFromOrder(orderId) {
                setTimeout(function () {
                    var OrderResource = $resource('rest/orders/:orderId', {orderId: orderId});
                    OrderResource.get().$promise.then(function (result) {

                        var copiedSettings = result.input;
                        $scope.nodeType = copiedSettings.nodeType;
                        clearSettingsWithNodeType($scope.nodeType)
                        $scope.settings.disk = copiedSettings.disks === '0' ? false : true;
                        console.log($scope.settings.disk);
                        _.each(copiedSettings, function (value, key) {
                            if (value !== null) {
                                $scope.settings[key] = value;
                            }
                        })
                    });
                }, 500);
            }

            function queryParameterIsValid(param) {
                return !_.isUndefined(param) && /^\d+$/.test(param);
            }

            function setDefaults() {
                if ($scope.currentUser && $scope.currentUser.superUser)
                    $scope.choices.defaults = defaults;
                else
                    $scope.choices.defaults = _.omit(defaults, "PLAIN_LINUX");

                if (_.isEmpty($scope.nodeType)){
                    clearSettingsWithNodeType('JBOSS');
                    $scope.nodeType = 'JBOSS';
                }

                $scope.busies = {};
                $scope.formErrors = {general: {}};
                $scope.formInfos = {};
                $scope.orderSent = false;
            }

            function retrieveUser() {
                $resource('/rest/users/:identifier').get({identifier: 'current'}, function (data) {
                    $scope.currentUser = data;
                    setDefaults();
                });
            }

            function clearErrorHandler(name) {
                $rootScope.$broadcast('GeneralError', {removeName: name});
            }

            function errorHandler(name, busyIndicator) {
                return function (data, status, headers, config) {
                    if (busyIndicator)
                        delete $scope.busies[busyIndicator];
                    $rootScope.$broadcast('GeneralError', {
                        name: name,
                        httpError: {data: data, status: status, headers: headers, config: config}
                    });
                };
            }

            function isLoggedInValidation() {
                if ($scope.currentUser && $scope.currentUser.authenticated) {
                    $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});
                    return true;
                } else {
                    $rootScope.$broadcast('GeneralError', {
                        name: 'Ikke logget inn',
                        message: 'Du må være innlogget for å legge inn en bestilling. Trykk \'i\' for å logge inn!'
                    });
                    return false;
                }
            }

            $scope.$watch('currentUser.authenticated', function (newVal, oldVal) {
                if (newVal) {
                    isLoggedInValidation();
                }
            });

            function validations() {
                return [
                    {
                        value: $scope.settings.environmentName,
                        target: ['environmentName_error'],
                        message: 'Miljønavn må spesifiseres'
                    },
                    {
                        value: $scope.settings.applicationMapping,
                        target: ['applicationMapping_error'],
                        message: 'Applikasjon/applikasjonsgruppe må spesifiseres'
                    },
                    {
                        value: $scope.settings.middleWareType,
                        target: ['middleWareType_error'],
                        message: 'Mellomvaretype må spesifiseres'
                    },
                    {
                        value: $scope.settings.commonDatasource,
                        target: ['commonDatasource_error'],
                        message: 'Datakilde for common må spesifiseres'
                    },
                    {
                        value: $scope.settings.cellDatasource,
                        target: ['cellDatasource_error'],
                        message: 'Datakilde for cell må spesifiseres'
                    },
                    {
                        value: $scope.settings.wasAdminCredential,
                        target: ['wasAdminCredential_error'],
                        message: 'WAS adminbruker må spesifiseres'
                    },
                    {
                        value: $scope.settings.ldapUserCredential,
                        target: ['ldapUserCredential_error'],
                        message: 'LDAP-bruker må spesifiseres'
                    },
                    {
                        value: $scope.settings.bpmServiceCredential,
                        target: ['bpmServiceCredential_error'],
                        message: 'BPM servicebruker må spesifiseres'
                    }
                ];
            }

            $scope.isValidForm = function () {
                _.each(validations(), function (validation) {
                    if (!_.isUndefined(validation.value)) {
                        util.withObjectInPath($scope.formErrors, validation.target, function (object, field) {
                            delete object[field];
                        });
                        if (!validation.value) {
                            util.withObjectInPath($scope.formErrors, validation.target, function (object, field) {
                                object[field] = validation.message;
                            });
                        }
                    }
                });
                return !hasValidationErrors() & isLoggedInValidation() && $rootScope.alive;
            };

            function hasValidationErrors() {
                return !_.chain($scope.formErrors).omit('general').isEmpty().value() || !_.isEmpty($scope.formErrors.general);
            }

            function checkForResolvedValdidationErrors() {
                _.each(validations(), function (validation) {
                    if (!_.isUndefined(validation.value) && !_.isEmpty(validation.value)) {
                        util.withObjectInPath($scope.formErrors, validation.target, function (object, field) {
                            delete object[field];
                        });
                    }
                });
            }

            $scope.isEmpty = function (object) {
                return _.isEmpty(object);
            }


            $scope.busies.environmentName = true;


            $http({
                method: 'GET',
                url: 'api/helper/fasit/environments',
                transformResponse: util.xmlTojson
            }).success(function (data) {

                $scope.choices.environments = _.chain(data.collection.environment).groupBy('envClass').map(function (e, k) {
                    delete $scope.busies.environmentName;
                    return [k, _.chain(e).map(function (e) {
                        return e.name;
                    }).sortBy(_.identity).value()];
                }).object().value();
            }).error(errorHandler('Miljøliste', 'environmentName'));


            $scope.busies.applicationMapping = true;

            $q.all([getApplications(), getApplicationGroups()]).then(function onSuccess(data) {

            var applications = toArray(data[0].data.collection.application);
            var applicationGroups = data[1].data;


            var filterAppsNotInAppGroup = function (application) {
                return application.applicationGroup === undefined;
            }

            var filterNonEmptyAppGrps = function (appGrp) {
                return appGrp.applications !== undefined;
            }

            var selectableApps = _.chain(applications).filter(filterAppsNotInAppGroup).map(mapAppInfo).value();
            var selectableAppGrps = _.chain(applicationGroups).filter(filterNonEmptyAppGrps).map(mapAppInfo).value();


            delete $scope.busies.applicationMapping;

            $scope.choices.applications = _.chain(selectableApps.concat(selectableAppGrps)).sortBy(
                function (obj) {
                    return obj.name.toLowerCase()
                }).value();

        }
);

$http({method: 'GET', url: 'rest/vm/choices'}).success(function (data) {
    _($scope.choices.serverSizes).each(function (serverSize, name) {
        _(serverSize).extend(data.serverSizes[name]);
    });
}).error(errorHandler('Valginformasjon'));

function getApplications() {
    return $http({method: 'GET', url: 'api/helper/fasit/applications', transformResponse: util.xmlTojson}).error(
        errorHandler('Applikasjonsliste', 'applicationMapping')
    );
}

function getApplicationGroups() {
    return $http({method: 'GET', url: 'api/helper/fasit/applicationGroups'}).error(
        errorHandler('Applikasjonsgruppeliste', 'applicationMapping')
    );
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

            // Trick to always get an array. Xml2json will make one item arrays into an object
            function toArray(obj) {
                return [].concat(obj);
            }

            function withDomain(f) {
                return $http({
                    method: 'GET',
                    url: 'rest/vm/domains',
                    params: {envClass: $scope.settings.environmentClass, zone: $scope.settings.zone}
                })
                    .success(f)
                    .error(errorHandler('Domener'));
            }

            function checkExistingResource() {
                var tasks = arguments;

                function condition(a) {
                    return a.condition === undefined || a.condition();
                }

                _.chain(tasks).filter(condition).each(function (task) {
                    withDomain(function (domain) {
                        $http({
                            method: 'GET',
                            url: 'api/helper/fasit/resources?bestmatch=true',
                            params: task.query(domain.domain)
                        })
                            .success(task.success)
                            .error(task.error);
                    });
                });
            }

            function baseQuery(domain) {
                return {
                    domain: domain,
                    envClass: $scope.settings.environmentClass,
                    envName: $scope.settings.environmentName,
                    app: $scope.settings.applicationMapping
                };
            }

            var checkDeploymentManagerDependency = {
                condition: function () {
                    return ($scope.nodeType === 'WAS_NODES' || $scope.nodeType === 'BPM_NODES')
                        && !_.isEmpty($scope.settings.environmentName);
                },
                query: function (domain) {
                    var alias = $scope.nodeType === 'WAS_NODES' ? 'wasDmgr' : 'bpmDmgr';
                    return _(baseQuery(domain)).extend({alias: alias, type: 'DeploymentManager'});
                },
                success: function (data) {
                	if (data.length === 0) {
                        $scope.formErrors.deploymentManager = ($scope.nodeType === 'WAS_NODES' ? 'WAS' : 'BPM') + ' Deployment Manager ikke funnet i gitt miljø og sone';
                    } else{
	                	clearErrorHandler('Deployment Manager');
	                    delete $scope.formErrors.deploymentManager;
                    }
                },
                error: function (data, status, headers, config) {
                   errorHandler('Deployment Manager')(data, status, headers, config);
                }
            };

            var checkRedundantDeploymentManager = {
                condition: function () {
                    return $scope.nodeType === 'WAS_DEPLOYMENT_MANAGER' || $scope.nodeType === 'BPM_DEPLOYMENT_MANAGER';
                },
                query: function (domain) {
                    var alias = $scope.nodeType === 'WAS_DEPLOYMENT_MANAGER' ? 'wasDmgr' : 'bpmDmgr';
                    return _(baseQuery(domain)).extend({alias: alias, type: 'DeploymentManager'});

                },
                success: function (data) {
                	 if (data.length === 0) {
                         clearErrorHandler('Deployment Manager');
                         delete $scope.formErrors.deploymentManager;
                     } else{
                    	 $scope.formErrors.deploymentManager = '' + $scope.choices.defaults[$scope.nodeType].nodeTypeName + ' eksisterer allerede i gitt miljø og sone';
                     }
                },
                error: function (data, status, headers, config) {
                    errorHandler('Deployment Manager')(data, status, headers, config);
                }
            };

            function enrichWithMultisite() {
                $resource('/rest/vm/domains/multisite').get({
                    envClass: $scope.settings.environmentClass,
                    envName: $scope.settings.environmentName
                }, function (data) {
                    $scope.formInfos.multisite = data.multisite;
                });
            }

            function clearSettingsWithNodeType(nodeType) {
                $scope.settings = _.omit($scope.choices.defaults[nodeType], 'nodeTypeName');
            }

            $scope.changeNodeType = function (nodeType) {
                clearSettingsWithNodeType(nodeType)
                $scope.settings.nodeType = nodeType;
                $scope.formErrors = {general: {}};
                $scope.formInfos = {};
                $rootScope.$broadcast('resetAllErrors');
                delete $scope.prepared;
            }

            if ((_.chain(defaults).keys().contains($routeParams.orderType).value())) {
                $scope.changeNodeType($routeParams.orderType);
                $scope.nodeType=$routeParams.orderType;
            }else if (queryParameterIsValid($routeParams.id)) {
                useSettingsFromOrder($routeParams.id);
            }


            $scope.changeZone = function (zone) {
                $scope.settings.zone = zone;
                checkExistingResource(checkDeploymentManagerDependency, checkRedundantDeploymentManager);
            }

            $scope.changeEnvironmentClass = function (environmentClass) {
                clearSettingsWithNodeType($scope.nodeType)
                $scope.settings.environmentClass = environmentClass;

                if ($scope.settings.environmentClass === 'u') {
                    $scope.settings.zone = 'fss';
                }

                $scope.formErrors = {general: {}};
                $scope.formInfos = {};
            }

            $scope.changeEnvironmentName = function (environmentName) {
                $scope.settings.environmentName = environmentName;
                enrichWithMultisite();
                checkExistingResource(checkDeploymentManagerDependency, checkRedundantDeploymentManager);
            }

            $scope.changeApplicationMapping = function () {
                checkExistingResource(checkDeploymentManagerDependency);
            }

            $scope.$watchCollection('settings', function () {
                if (!_.chain($scope.formErrors).omit('general').isEmpty().value()) {
                    checkForResolvedValdidationErrors();
                }
            });

            $scope.prepSave = function (statusText) {
                $scope.status = statusText;
            };

            function onOrderSuccess(order) {
                delete $scope.busies.orderSend;
                $location.path('/order_details/' + order.id)
            }

            function onOrderError(data, status, headers, config) {
                delete $scope.orderSent;
                errorHandler('Ordreinnsending', 'orderSend')(data, status, headers, config);
            }

            $scope.submitOrder = function () {
                clearErrorHandler('Ordreinnsending');
                if ($scope.isValidForm()) {
                    setDisks();
                    $scope.settings.nodeType = $scope.nodeType;
                    $scope.orderSent = true;
                    $scope.busies.orderSend = true;
                    if ($scope.prepared && $scope.prepared) {
                        $http.put('rest/orders/' + $scope.prepared.orderId, $scope.prepared.xml, {
                            headers: {'Content-type': 'text/plain', 'Accept': 'application/json'}
                        }).success(onOrderSuccess).error(onOrderError);
                    } else {
                        $http.post('rest/orders',_.omit($scope.settings, 'sugar'))
                            .success(onOrderSuccess).error(onOrderError);
                    }
                }
            };

            $scope.editXML = function () {
                if ($scope.isValidForm()) {
                    setDisks();
                    $scope.settings.nodeType = $scope.nodeType;
                    $scope.busies.orderPrepare = true;
                    $http.post('rest/orders?prepare=true', _.omit($scope.settings, 'sugar')).success(function (order) {
                        delete $scope.busies.orderPrepare;
                        $scope.prepared = {xml: order.externalRequest, orderId: order.id};
                    }).error(errorHandler('Ordreinnsending', 'orderSend'));
                }
            };

            function setDisks() {
                if ($scope.settings.disk) {
                    $scope.settings.disks = 1;
                } else {
                    $scope.settings.disks = 0;
                }
                delete $scope.settings.disk;

            }


        }];
