'use strict';

module.exports = ['$resource', function($resource) {
        return $resource('rest/system/notifications/active', {}, {
           query: {method:'GET',params:{},isArray:true}
        });
    }];
