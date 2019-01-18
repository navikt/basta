'use strict';

module.exports = ['$http', 'errorService', 'BastaService', "$rootScope", '$routeParams',
    function ($http, errorService, BastaService, $rootScope, $routeParams) {

        this.data = {
            environmentClass: $routeParams.environmentClass || 'u',
            environmentName: $routeParams.environmentName,
            application: $routeParams.application,
            queueName: $routeParams.queueName,
            queueManager: undefined,
            fasitAlias: undefined,
            mqQueueName: null,
            maxMessageSize: 4,
            queueDepth: 5000,
            createBackoutQueue: false,
            clusterName: undefined,
            backoutThreshold: 1,
        }
        this.creates = [];
        this.validation = {};
        this.inEditQueueNameMode = false;
        var ctrl = this;

        function init() {
            generateFasitAlias();
            resetValidation();
            generateQueueName();
            updateQueueMananger();
        }

        this.changeApplication = function () {
            generateFasitAlias();
            resetValidation();
            generateQueueName();
            updateQueueMananger();
        }

        this.changeEnvironmentClass = function () {
            delete this.data.environmentName;
            delete this.data.queueManager;
            resetValidation();
            generateQueueName();
            updateQueueMananger()
        }

        this.changeEnvironment = function () {
            resetValidation();
            generateQueueName();
            updateQueueMananger();
        }

        this.changeQueueName = function () {
            generateFasitAlias();
            generateQueueName();
            resetValidation();
        }

        this.changeFasitAlias = function () {
            resetValidation();
            generateQueueName();
        }

        this.changeMqQueueName = function () {
            resetValidation();
        }

        this.changeQueueManager = function () {
            updateClusters();
        }

        function updateQueueMananger() {
            if (ctrl.data.environmentName && ctrl.data.application) {
                $rootScope.$broadcast('UpdateQueueManagerEvent', ctrl.data.queueManager);
                updateClusters();
            }
        }

        function updateClusters() {
            if (ctrl.data.queueManager) {
                $rootScope.$broadcast('UpdateClustersEvent', ctrl.data.clusterName);
            }
        }

        function generateFasitAlias() {
            if (ctrl.inEditFasitAliasMode) {
                console.log("Will not generate new fasitAlias in editmode");
                return;
            }
            ctrl.data.fasitAlias = ctrl.data.application + "_" + ctrl.data.queueName;
        }

        function generateQueueName() {
            if (ctrl.inEditQueueNameMode) {
                console.log("Will not generate new queuename in editmode");
                return;
            }
            var env = '';
            if (ctrl.data.environmentName)
                env = ctrl.data.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + "_";
            var app = '';
            if (ctrl.data.application)
                app = ctrl.data.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + ".";
            var name = '';
            if (ctrl.data.queueName) {
                name = ctrl.data.queueName.toUpperCase().replace(/[^A-Z0-9._]/g, '');
            }

            ctrl.data.mqQueueName = env + app + name;
        }

        // validate promise
        function validate(callback) {
//				console.log("validating");
            ctrl.processing = true;
            if (ctrl.data.environmentClass && ctrl.data.environmentName && ctrl.data.application && ctrl.data.queueManager) {

                $http.put("rest/v1/mq/order/queue/validate", ctrl.data).then(function (response) {
                    console.log("response", response.data);
                    ctrl.validation = response.data;
                    ctrl.validation.mqerror = hasMqValidationError();
                    var mqDetails = []
                    if (response.data.local_queue) {
                        mqDetails.push("Lokal kø finnes allerede i MQ")
                    }
                    if (response.data.backout_queue) {
                        mqDetails.push("Backout kø finnes allerede i MQ")
                    }
                    if (response.data.alias_queue) {
                        mqDetails.push("Kø alias finnes allerede i MQ")
                    }
                    ctrl.validation.mqErrorDetails = mqDetails;

                    callback()
                }, errorService.handleHttpError('Validation'));
            } else {
                console.log("noe er ikke satt")
                callback()
            }
        }

        function hasMqValidationError() {
            return ctrl.validation.local_queue || ctrl.validation.backout_queue || ctrl.validation.alias_queue
        }

        function hasValidationError() {
            return ctrl.validation.fasit || hasMqValidationError();
        }

        function resetValidation() {
            ctrl.validation = {};
        }

        this.sendOrder = function () {
            if (hasValidationError()) {
                console.log("We have validation errors", ctrl.validation)
                ctrl.processing = false;
            } else {
                console.log("Posting mq queue order", ctrl.data)
                BastaService.postOrder('rest/v1/mq/order/queue', ctrl.data);
            }

        }

        this.submitOrder = function () {
            validate(ctrl.sendOrder);
        };

        init();

    }];
