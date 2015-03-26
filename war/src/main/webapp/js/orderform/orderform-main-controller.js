'use strict';



angular.module('basta.orderform-main-controller', [])
    .controller('orderformMainController', function ($scope, $http, $location,User, errorService ) {

        var setAuthenticated = function (auth) {
            this.authenticated = auth;
            if (!auth){this.changeEnvironmentClass();
            }
        };

        $scope.$on('UserUpdated', function(){
            User.authenticated().then(setAuthenticated.bind(this));
        }.bind(this));

        User.authenticated().then(setAuthenticated.bind(this));

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
            console.log(this.master);

        };

        this.changeEnvironment = function(data,y){
             console.log(data,y);
            console.log(this.data.properties.environmentName);
        }


        this.changeEnvironmentClass = function(){
            this.data.properties={};
            if (this.data.envClass === 'u') {
                this.data.zone = 'fss';
            }
            $scope.form.$setPristine();


        }




    });

