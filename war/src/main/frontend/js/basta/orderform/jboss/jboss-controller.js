'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', function($scope, User) {

    var vm = this;

    vm.data = {
	nodeType : 'JBOSS',
	middleWareType : 'jb',
	environmentClass : 'u',
	zone : 'fss',
	properties : {
	    "disks" : "0",
	    "serverSize" : "s",
	    "serverCount" : "1"
	}
    }

    var setAuthenticated = function(auth) {
	vm.authenticated = auth;
	if (!auth) {
	    changeEnvironmentClass();
	}
    };

    var setSuperuser = function(sudo) {
	vm.superuser = sudo;
    };

    User.onchange(function() {
	var user = User.current();
	setAuthenticated(user.authenticated);
	setSuperuser(user.superUser);

    });
 

    function changeEnvironmentClass() {
	delete vm.data.properties.environmentName;
	delete vm.config;
	if (vm.data.envClass === 'u') {
	    vm.data.zone = 'fss';
	}

    }

    this.validate = function(data) {
	if ($scope.form.$valid) {
	    this.master = angular.copy(data);
	}

    };

    this.changeEnvironmentClass = function() {
	changeEnvironmentClass();
	$scope.form.$setPristine();
    }

} ];
