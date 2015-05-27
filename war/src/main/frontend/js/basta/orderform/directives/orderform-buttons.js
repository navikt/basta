'use strict';

module.exports = ['User',  function (User) {
        var isSuperUser = function (superuser) {
            this.superuser = superuser;
        };

        return {
            restrict: 'E',
            scope: {
                formvalid: '=',
                onSubmit: '&',
            },
            link: function(scope, el, attr) {
            	scope.submitEnabled=angular.isDefined(attr.onSubmit);
            },

            controller: ['$scope', function ($scope) {
                $scope.$on('UserUpdated', function(){
                    User.sudo().then(isSuperUser.bind(this));
                }.bind(this));
                User.sudo().then(isSuperUser.bind(this));
                this.busy=false;
                
                this.submit = function(){
                    if(this.formvalid){
                    	this.busy=true;
                    	this.onSubmit();
                    }else {
                    	console.log('form is not valid. Check errors ')	
                    };
                }
               
            }],
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-buttons.html"
        };
    }];

