'use strict';

angular.module('basta.notification_service', [])
    .factory('notificationService', ['$resource', function($resource) {

        return $resource('rest/system/notifications/active', {}, {
           query: {method:'GET',params:{},isArray:true}
        });
    }]);
