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

        $scope.$on('UserUpdated', function(){
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
            properties : {}
        }


        this.validate = function(data) {
            if($scope.form.$valid){
                this.master = angular.copy(data);
            };

        };

        this.changeEnvironmentClass = function(){
            this.data.properties={};
            if (this.data.envClass === 'u') {
                this.data.zone = 'fss';
            }
            $scope.form.$setPristine();


        }




    }];
