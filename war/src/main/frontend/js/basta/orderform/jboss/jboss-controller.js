'use strict';

var angular = require('angular');

module.exports = ['$scope', '$http', '$location', 'User', 'errorService', function ($scope, $http, $location,User, errorService ) {

        var setAuthenticated = function (auth) {
            this.authenticated = auth;
            if (!auth){this.changeEnvironmentClass();
            }
        };

        var setSuperuser = function (sudo) {
            this.superuser = sudo;
        };

        $scope.$on('UserChanged', function(){
            User.authenticated().then(setAuthenticated.bind(this));
            User.sudo().then(setSuperuser.bind(this));
        }.bind(this));

        User.authenticated().then(setAuthenticated.bind(this));
        User.sudo().then(setSuperuser.bind(this));


        this.data={
            nodeType: 'JBOSS',
            middleWareType: 'jb',
            environmentClass: 'u',
            zone:'fss',
            "properties": {
                "disks": "0",
                "serverSize": "s",
                "serverCount": "1"
            }
        }


        this.validate = function(data) {
            if($scope.form.$valid){
                this.master = angular.copy(data);
            };

        };

        this.changeEnvironmentClass = function(){
            delete this.data.properties.environmentName;
            delete this.config;
            if (this.data.envClass === 'u') {
                this.data.zone = 'fss';
            }

            $scope.form.$setPristine();


        }




    }];

