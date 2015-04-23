'use strict';



module.exports = ['$http', 'errorService', function($http,errorService) {

        this.current = function(){
             return $http({method: 'GET', cache:false, url: 'rest/users/current'}).then(function(response){
               return response.data;
             });
        };

        this.su = function(){
            return $http({method: 'GET', cache:false, url: 'rest/users/current'}).then(function(response){
                return (!_.isUndefined(response.data) && response.data.authenticated && response.data.superUser);
            });
        };

        this.authenticated = function(){
            return $http({method: 'GET', cache:false, url: 'rest/users/current'}).then(function(response){
                return (!_.isUndefined(response.data) && response.data.authenticated);
            });
        };

        return {
            current: this.current,
            sudo: this.su,
            authenticated: this.authenticated
        }
    }];
