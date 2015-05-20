'use strict';

module.exports = ['User', 'BastaService', function (User, BastaService) {
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
                this.inEditorMode=false;
                
                this.submit = function(){
                    if(this.formvalid){
                    	console.log('submitting form');
                    	this.busy=true;
                    	this.onSubmit();
                    	this.busy=false;
                    }else{
                    	console.log('form is not valid. Check errors ')	
                    };
                }
               
            }],
            controllerAs: 'ctrl',
            bindToController: true,
            templateUrl: "basta/orderform/directives/orderform-buttons.html"
        };
    }];

