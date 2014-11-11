'use strict';

angular.module('skyBestApp.notification_service', [])
    .factory('notificationService', ['$resource', function($resource) {

        return $resource('rest/system/notifications/active', {}, {
           query: {method:'GET',params:{},isArray:true}
        });
    }]);
