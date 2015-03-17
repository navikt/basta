'use strict';

angular.module('basta.user_service', [])
    .factory('User', ['$resource', function($resource) {
        return $resource('/rest/users/current', {}, {
           currentUser: {method:'GET',params:{},isArray:false}
        });
    }]);
