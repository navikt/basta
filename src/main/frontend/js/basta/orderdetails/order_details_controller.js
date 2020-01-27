'use strict';
var moment = require('moment');
module.exports = ['$scope', '$http', '$resource', '$routeParams', '$location', '$interval', '$rootScope', '$timeout', 'errorService', 'User','notificationService',
        function ($scope, $http, $resource, $routeParams, $location, $interval, $rootScope, $timeout, errorService, User, notificationService) {

            $scope.model = {
                exists: false,
                showXML: false,
                routeParamsId: $routeParams.id
            }

            $scope.model.notes =  notificationService.query();

            $scope.selectedNodes = null;

            var OrderResource = $resource('rest/orders/:orderId', {orderId: '@id'});
            var OrderLogs = $resource('rest/orders/:orderId/statuslog', {orderId: '@id'});

            $scope.hasEnvironmentClassAccess = function () {
                return User.hasEnvironmentClassAccess( $scope.orderDetails.input.environmentClass);
            };

            $scope.polling = false;
            var max = 1;
            refresh(0);
            function refresh(times) {
                if (times >= max) {
                    $scope.stopPoll();
                } else {
                    OrderLogs.query({orderId: $routeParams.id}).
                        $promise.then(
                        function (value) {
                            $scope.statusLog = value;
                        }
                    );
                    OrderResource.get({orderId: $routeParams.id})
                        .$promise.then(
                        function (value) {
                            $scope.model.exists = true;
                            $scope.orderDetails = value;
                            function getType(order) {
                                if (_.isEmpty(order.orderDescription)) {
                                    return  _(order.orderOperation).humanize();
                                }
                                return  _(order.orderOperation).humanize() + " | " + _(order.orderType).humanize() + " | " + _(order.orderDescription).chain().humanize().titleize().value();
                            }

                            function getOrderOperation(order) {

                                return  _(order.orderOperation).chain().humanize().titleize().value();
                            }

                            $scope.orderDetails.type = getType(value);
                            $scope.orderDetails.orderTypeHumanized = getOrderOperation(value);
                            $scope.model.existingNodes = nodesWithStatus('DECOMMISSIONED', true);
                            $scope.model.startedNodes = nodesWithStatus('ACTIVE');
                            $scope.model.stoppedNodes = nodesWithStatus('STOPPED');

                            function shouldStartPollAutomatically() {
                                var iscreatedLessThan40minutesAgo = moment().subtract(40, 'minutes').isBefore(moment(value.created));
                                var statusInProgress = ( $scope.orderDetails.status === 'PROCESSING' || $scope.orderDetails.status === 'NEW' || $scope.orderDetails.status === 'WAITING');
                                return iscreatedLessThan40minutesAgo && statusInProgress && $scope.polling === false;
                            }

                            if (shouldStartPollAutomatically()) {
                                $scope.startPoll();
                                $scope.automaticallyStarted = true;
                            }

                            function shouldStopPollAutomatically() {
                                return $scope.automaticallyStarted &&
                                    ($scope.orderDetails.status === 'SUCCESS' || $scope.orderDetails.status === 'ERROR');

                            }

                            if (shouldStopPollAutomatically()) {
                                $scope.automaticallyStarted = false;
                                $scope.stopPoll();
                            }
                        },
                        function (error) {
                            $scope.model.exists = false;
                        }
                    )
                }
            }

            max = 800;
            var sleep = 3000;
            $scope.from = (max * sleep) / 1000;
            var poll;
            $scope.startPoll = function () {
                refresh(0);
                if (angular.isDefined(poll)) return;
                poll = $interval(refresh, sleep, max + 1);
                $scope.$broadcast('timer-start');
                $scope.polling = true;
            }
            $scope.stopPoll = function () {
                $interval.cancel(poll);
                poll = undefined;
                $scope.$broadcast('timer-stop');
                $scope.$broadcast('timer-set-countdown', $scope.from);
                $scope.polling = false;
                $scope.stopPolledCalled = true;
            }

            $scope.$on("timer-stopped", function () {
                $scope.polling = false;
            })

            $scope.$on('$destroy', function () {
                $scope.stopPoll();
            });

            $scope.filterDate = function (item) {
                if ($scope.searchDate) {
                    var d = moment(item.created).format('YYYY-MM-DD HH:mm:ss');
                    return (d).indexOf($scope.searchDate) != -1;
                }
                return true;
            }

            function nodesWithStatus(status, inverse) {
                return _.chain($scope.orderDetails.resultDetails)
                    .filter(function (node) {
                        if (inverse) {
                            return node.details.nodeStatus != status;
                        } else {
                            return node.details.nodeStatus === status
                        }
                    })
                    .map(function (node) {
                        return node.resultName;
                    })
                    .value();
            }
            
          
            
            function findOrderPath (){


                if (!$scope.orderDetails){
            		return ;
            	}
            	  var orderMap={
                    		certificate:'serviceuser_certificate_order',
                    		credential:'serviceuser_credential_order', 
                    		jboss:'jboss_order',
                    		liberty:'liberty_order',
                    		openam_server:'openam_server_order',          
                    		openam_proxy:'openam_proxy_order',
                            plain_linux: 'linux_order',
                            dev_tools: 'devtools_order',
                            container_linux: 'containerlinux_order',
                    		windows_applicationserver:'windows_order',                
                    		was_nodes:'was_node_order',               
                    		was_deployment_manager:'was_dmgr_order',               
                    		bpm_nodes:'bpm_node_order',               
                    		bpm_deployment_manager:'bpm_dmgr_order',               
                    		oracle:'oracle_order',
                    		"big-ip config":'bigip_order',
                    		queue:'mq_queue_order',    			
                    		channel:'mq_channel_order'}
            	var type = $scope.orderDetails.orderDescription.toLowerCase();
				return orderMap[type];
            }
            
            $scope.showCopyButton= function(){
            	if(!User.isAuthenticated()){
            		return false;
            	}
            	if (!$scope.orderDetails){
            		return false;
            	}
            	if($scope.orderDetails.orderOperation ==='CREATE' && findOrderPath()){
            		return true;
            	}
            	return false;
            	
            }

            $scope.copyOrder = function () {
            	var path=findOrderPath();
            	if(path){
            		$location.path(path).search($scope.orderDetails.input);
            	}else{
            		console.log("can not copy with unknown path");
            	}
            }
            
            $scope.findResultTemplate = function(){
            	if (!$scope.orderDetails){
            		return ;
            	}
            	
            	var template;
            	var orderType = $scope.orderDetails.orderType;
	            switch (orderType) {
				case 'VM':
					template='result-vm.html';
					break;
				case 'ServiceUser':
					template='result-serviceuser.html';
					break;
				case 'MQ':
					template='result-mq.html';
					break;
				default:
					template='result-simple.html';
				}
	            return 'basta/orderdetails/templates/'+ template; 
            		
            }

        }];