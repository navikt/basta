'use strict';

angular.module('skyBestApp.order_form_controller', [])
    .controller('orderFormController', ['$scope', '$rootScope', '$http', '$routeParams', '$resource', '$location', '$templateCache', function ($scope, $rootScope, $http, $routeParams, $resource, $location, $templateCache) {

        function setDefaults() {
            if ($scope.currentUser && $scope.currentUser.superUser)
                $scope.choices.defaults = defaults;
            else
                $scope.choices.defaults = _.omit(defaults, "PLAIN_LINUX");
        }

        function retrieveUser() {
            $resource('/rest/users/:identifier').get({identifier: 'current'}, function (data) {
                $scope.currentUser = data;
                setDefaults();
            });
        }

        retrieveUser();
        $scope.$on('UserChanged', retrieveUser);

        $scope.choices = {
            zones: ['fss', 'sbs'],
            environmentClasses: ['u', 't', 'q', 'p'],
            environmentClassNames: {u: 'Utvikling', t: 'Test', q: 'PreProd', p: 'Produksjon'},
            serverCounts: [1, 2, 4, 8],
            serverSizes: {s: {name: 'Standard'}, m: {name: 'Medium'}, l: {name: 'Stor'}},
            middleWareTypeMessages: {}
        };

        setDefaults();


        $scope.nodeType = 'APPLICATION_SERVER';
        $scope.busies = {};
        $scope.formErrors = { general: {} };
        $scope.formInfos = {};
        $scope.orderSent = false;

        function clearErrorHandler(name) {
            $rootScope.$broadcast('GeneralError', { removeName: name });
        }

        function errorHandler(name, busyIndicator) {
            return function (data, status, headers, config) {
                if (busyIndicator)
                    delete $scope.busies[busyIndicator];
                $rootScope.$broadcast('GeneralError', { name: name, httpError: { data: data, status: status, headers: headers, config: config }});
            };
        }


        function isLoggedInValidation(){
            if ($scope.currentUser && $scope.currentUser.authenticated){
                $rootScope.$broadcast('GeneralError', {removeName: 'Ikke logget inn'});
                return true;
            }else{
                $rootScope.$broadcast('GeneralError', {name: 'Ikke logget inn', message: 'Du må være innlogget for å legge inn en bestilling. Trykk \'i\' for å logge inn!'});
                return false;
            }
        }

        $scope.$watch('currentUser.authenticated', function (newVal, oldVal) {
            if (newVal){
                isLoggedInValidation();
            }
        });

        function validations(){
            return [
                { value: $scope.settings.environmentName, target: ['environmentName_error'], message: 'Miljønavn må spesifiseres' },
                { value: $scope.settings.applicationName, target: ['applicationName_error'], message: 'Applikasjonsnavn må spesifiseres'},
                { value: $scope.settings.middleWareType, target: ['middleWareType_error'], message: 'Mellomvaretype må spesifiseres' },
                { value: $scope.settings.commonDatasource, target: ['commonDatasource_error'], message: 'Datakilde for common må spesifiseres' },
                { value: $scope.settings.cellDatasource, target: ['cellDatasource_error'], message: 'Datakilde for cell må spesifiseres' },
                { value: $scope.settings.wasAdminCredential, target: ['wasAdminCredential_error'], message: 'WAS adminbruker må spesifiseres'},
                { value: $scope.settings.ldapUserCredential, target: ['ldapUserCredential_error'], message: 'LDAP-bruker må spesifiseres'},
                { value: $scope.settings.bpmServiceCredential, target: ['bpmServiceCredential_error'], message: 'BPM servicebruker må spesifiseres'}
            ];
        }

        $scope.isValidForm = function () {
            _.each(validations(), function (validation) {
                if (!_.isUndefined(validation.value)) {
                    withObjectInPath($scope.formErrors, validation.target, function (object, field) {
                        delete object[field];
                    });
                    if (!validation.value) {
                        withObjectInPath($scope.formErrors, validation.target, function (object, field) {
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

        function checkForResolvedValdidationErrors (){
            _.each(validations(), function (validation) {
                if (!_.isUndefined(validation.value) && !_.isEmpty(validation.value)) {
                    withObjectInPath($scope.formErrors, validation.target, function (object, field) {
                        delete object[field];
                    });
                }
            });
        }

        $scope.isEmpty = function (object) {
            return _.isEmpty(object);
        };
        $scope.hasZone = function (zone) {
            return !(zone === 'sbs' && $scope.settings.environmentClass === 'u');
        };
        $scope.hasEnvironmentClassAccess = function (environmentClass) {
            if ($scope.currentUser) {
                var classes = $scope.currentUser.environmentClasses;
                return classes.indexOf(environmentClass) > -1;
            }
            return false;
        };


        $scope.busies.environmentName = true;

        $http({ method: 'GET', url: 'api/helper/fasit/environments', transformResponse: xml2json }).success(function (data) {

            $scope.choices.environments = _.chain(data.collection.environment).groupBy('envClass').map(function (e, k) {
                delete $scope.busies.environmentName;
                return [k, _.chain(e).map(function (e) {
                    return e.name;
                }).sortBy(_.identity).value()];
            }).object().value();
        }).error(errorHandler('Miljøliste', 'environmentName'));

        $scope.busies.applicationName = true;
        $http({ method: 'GET', url: 'api/helper/fasit/applications', transformResponse: xml2json }).success(function (data) {
            delete $scope.busies.applicationName;
            $scope.choices.applications = _.chain(data.collection.application).map(function (a) {
                return a.name;
            }).sortBy(_.identity).value();
        }).error(errorHandler('Applikasjonsliste', 'applicationName'));

        $http({ method: 'GET', url: 'rest/choices' }).success(function (data) {
            _($scope.choices.serverSizes).each(function (serverSize, name) {
                _(serverSize).extend(data.serverSizes[name]);
            });
        }).error(errorHandler('Valginformasjon'));

        function withDomain(f) {
            return $http({ method: 'GET', url: 'rest/domains', params: {envClass: $scope.settings.environmentClass, zone: $scope.settings.zone}})
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
                    $http({ method: 'GET', url: 'api/helper/fasit/resources/bestmatch', params: task.query(domain), transformResponse: xml2json })
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
                app: $scope.settings.applicationName
            };
        }

        var checkDeploymentManagerDependency = {
            condition: function () {
                return ($scope.nodeType === 'WAS_NODES' || $scope.nodeType === 'BPM_NODES')
                    && !_.isEmpty($scope.settings.environmentName);
            },
            query: function (domain) {
                var alias = $scope.nodeType === 'WAS_NODES' ? 'wasDmgr' : 'bpmDmgr';
                return _(baseQuery(domain)).extend({ alias: alias, type: 'DeploymentManager' });
            },
            success: function (data) {
                clearErrorHandler('Deployment Manager');
                delete $scope.formErrors.deploymentManager;
            },
            error: function (data, status, headers, config) {
                if (status === 404) {
                    $scope.formErrors.deploymentManager = ($scope.nodeType === 'WAS_NODES' ? 'WAS' : 'BPM') +  ' Deployment Manager ikke funnet i gitt miljø og sone';
                } else
                    errorHandler('Deployment Manager')(data, status, headers, config);
            }
        };

        var checkRedundantDeploymentManager = {
            condition: function () {
                return $scope.nodeType === 'WAS_DEPLOYMENT_MANAGER' || $scope.nodeType === 'BPM_DEPLOYMENT_MANAGER';
            },
            query: function (domain) {
                 var alias = $scope.nodeType === 'WAS_DEPLOYMENT_MANAGER' ? 'wasDmgr' : 'bpmDmgr';
                 return _(baseQuery(domain)).extend({ alias: alias, type: 'DeploymentManager' });

            },
            success: function (data) {
                $scope.formErrors.deploymentManager = '' + $scope.choices.defaults[$scope.nodeType].nodeTypeName  + ' eksisterer allerede i gitt miljø og sone';
            },
            error: function (data, status, headers, config) {
                if (status === 404) {
                    clearErrorHandler('Deployment Manager');
                    delete $scope.formErrors.deploymentManager;
                } else errorHandler('Deployment Manager')(data, status, headers, config);
            }
        };


        function enrichWithMultisite() {
            $resource('/rest/domains/multisite').get({envClass: $scope.settings.environmentClass, envName: $scope.settings.environmentName}, function (data) {
                $scope.formInfos.multisite =  data.multisite;
            });
        }



        $scope.$watch('nodeType', function (newVal, oldVal) {
            $scope.settings = _.omit($scope.choices.defaults[newVal], 'nodeTypeName');
            $scope.settings.nodeType = newVal;
            $scope.formErrors = { general: {} };
            $scope.formInfos = {};
            delete $scope.prepared;
        });

        $scope.$watch('settings.zone', function (newVal, oldVal) {
            if (newVal === oldVal) {
                return;
            }
            checkExistingResource(checkDeploymentManagerDependency, checkRedundantDeploymentManager);
        });

        $scope.$watchCollection('settings', function () {
            if (!_.chain($scope.formErrors).omit('general').isEmpty().value()) {
                checkForResolvedValdidationErrors();
            }
        });

        $scope.$watch('settings.environmentName', function (newVal, oldVal) {
            if (newVal === oldVal) {
                return;
            }
            if (newVal){
               enrichWithMultisite();
            }else{
                $scope.formInfos.multisite =  false;
            }
            checkExistingResource(checkDeploymentManagerDependency, checkRedundantDeploymentManager);
        });

        $scope.$watch('settings.applicationName', function (newVal, oldVal) {
            if (newVal === oldVal) {
                return;
            }
            checkExistingResource(checkDeploymentManagerDependency);
        });

        $scope.$watch('settings.environmentClass', function (newVal, oldVal) {
            if (newVal === oldVal) {
                return;
            }
            $scope.settings = _.omit($scope.choices.defaults[$scope.nodeType], 'nodeTypeName');
            $scope.settings.environmentClass = newVal;

            if ($scope.settings.environmentClass === 'u') {
                $scope.settings.zone = 'fss';
            }
            $scope.formErrors = { general: {} };
            $scope.formInfos = {};
        });

        $scope.prepSave = function (statusText) {
            $scope.status = statusText;
        };

        function onOrderSuccess(order) {
            delete $scope.busies.orderSend;
            $location.path('/order_list').search({ id: order.id });
        }

        function onOrderError(data, status, headers, config) {
            delete $scope.orderSent;
            errorHandler('Ordreinnsending', 'orderSend')(data, status, headers, config);
        }

        $scope.submitOrder = function () {
            if ($scope.isValidForm()) {
                setDisks();
                $scope.settings.nodeType = $scope.nodeType;
                $scope.orderSent = true;
                $scope.busies.orderSend = true;
                if ($scope.prepared && $scope.prepared.xml) {
                    $http.put('rest/orders/' + $scope.prepared.orderId, $scope.prepared.xml, {
                        headers: {'Content-type': 'text/plain', 'Accept': 'application/json'}
                    }).success(onOrderSuccess).error(onOrderError);
                } else {
                    $http.post('rest/orders', $scope.settings)
                        .success(onOrderSuccess).error(onOrderError);
                }
            }
        };

        $scope.editXML = function () {
            if ($scope.isValidForm()) {
                setDisks();
                $scope.settings.nodeType = $scope.nodeType;
                $scope.busies.orderPrepare = true;
                $http.post('rest/orders?prepare=true', $scope.settings).success(function (order) {
                    delete $scope.busies.orderPrepare;
                    $scope.prepared = {xml: order.requestXml, orderId: order.id};
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
    }]);
