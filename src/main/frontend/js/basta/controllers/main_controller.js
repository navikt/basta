'use strict';
var util = require('../utils/util');
require('angular-hotkeys');
module.exports = [ '$scope', '$rootScope', '$http', '$templateCache', '$location', '$resource', 'notificationService', 'User', 'hotkeys',
		'$interval', function($scope, $rootScope, $http, $templateCache, $location, $resource, notificationService, User, hotkeys, $interval) {

			hotkeys.add({
				combo : 'h',
				description : 'History',
				callback : function() {
					$location.url('order_list');
				}
			});

			hotkeys.add({
				combo : 'c',
				description : 'Create new order',
				callback : function() {
					$location.url('menu');
				}
			});

			function handleAndDisplayRelevantVersionInfo() {
				$http.get('/version').then(function(data) {
					$scope.version = data.data;
					if (itIsANewVersionForUser($scope.version)) {
						$http.get('/changelog/' + $scope.version + '.md').success(function(data) {
							localStorage.setItem('version', $scope.version);
							$location.url('changelog?version=' + $scope.version + "&firstTime");
						});
					}
				});
			}

			var itIsANewVersionForUser = function(version) {
				var storedVersion = localStorage.getItem('version');
				return _.isUndefined(storedVersion) || storedVersion != version;
			};

			$http({
				method : 'GET',
				url : 'rest/datasource'
			}).success(function(data) {
				$scope.datasource = data.datasource;
			})

			$scope.createTemplate = function() {
				$location.path('/template');
			};

			function isBlocking(notifications) {
				notifications.$promise.then(function(notes) {
					$scope.isAnyBlockingNotifications = _.any(notes, function(note) {
						return note.blockOperations === true;
					});
				});
			}

			$scope.notifications = notificationService.query();
			isBlocking($scope.notifications);

			$scope.$on('notification:updated', function() {
				$scope.notifications = notificationService.query();
				isBlocking($scope.notifications);
			});

			$scope.location = $location;
			User.onchange(function() {
				$scope.currentUser = User.current();
			})
			handleAndDisplayRelevantVersionInfo();
			util.initTooltips();
		} ];
