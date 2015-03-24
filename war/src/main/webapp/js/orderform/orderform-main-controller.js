'use strict';



angular.module('basta.orderform-main-controller', [])
    .controller('orderformMainController', function ($scope, User) {

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
            envClass: 'u',
            zone:'fss',
            properties : {
                application:'',
                environment:''
            }
        }


        this.validate = function(data) {
            console.log("HOI");
            console.log(data);
            this.master = angular.copy(data);
        };


        this.changeEnvironmentClass = function(){
            this.data.properties={};
            if (this.data.envClass === 'u') {
                this.data.zone = 'fss';
            }
        }




    });

