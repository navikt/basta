'use strict';

angular.module('skyBestApp.notification_service', [])
    .factory('notificationService', ['$resource', function($resource) {

        var service =  $resource('rest/system/notifications/active', {}, {
           query: {method:'GET',params:{},isArray:true}
        });

        return service;
    }]);
