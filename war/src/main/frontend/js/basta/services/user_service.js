'use strict';

module.exports = [ '$http', 'errorService', '$rootScope','$interval', '$location', function($http, errorService, $rootScope, $interval, $location) {

    var AUTH_EVENT= 'Autentiseringsfeil';
    var currentUser= {};
    
    init();
    
    function init(){
	updateCurrentUser();
	$interval(isUserChanged, 10000);
    }
    
	
    function getUserPromise() {
	return $http({
	    method : 'GET',
	    cache : false,
	    url : 'rest/users/current'
	})
	.catch( errorHandler);
    }
    
    function updateCurrentUser(){
	 getUserPromise().then(function(response) {
	     if(!_.isUndefined(response)){
		currentUser=response.data;
	    }
	});
    }

    
    function isUserChanged() {
	function isSameUser(oldUser, newUser) {
	    var fields = [ 'authenticated', 'roles', 'username', 'superUser' ];
	    return _.isEqual(_.pick(newUser, fields), _.pick(oldUser, fields));
	}
	 getUserPromise().then(function(response) {
	    if (!_.isUndefined(currentUser) && !isSameUser(response.data, currentUser)) {
		console.log("User has changed");
		userChanged();
	    }
	});
    }
    
    function userChanged(){
	updateCurrentUser();
	$rootScope.$broadcast('UserChanged');
    }
    
    
    this.current = function() {
	return getUserPromise().then(function(response) {

	    if(_.isUndefined(response)){
		return {name:'unauthenticated', displayName:'unauthenticated'}
	    }else{
		$rootScope.$broadcast('GeneralError', { removeName : AUTH_EVENT});
	    }
	    return response.data;
	});
    };

    this.su = function() {
	return getUserPromise().then(function(response) {
	    return (!_.isUndefined(response.data) && response.data.authenticated && response.data.superUser);
	});
    };

    this.authenticated = function() {
	return getUserPromise().then(function(response) {
	    return (!_.isUndefined(response.data) && response.data.authenticated);
	});
    };

    this.login = function(username, password) {
	// console.log("logging in user", username);
	var config = {
	    headers : {
		'Content-Type' : 'application/x-www-form-urlencoded'
	    }
	};
	var data = $.param({
	    j_username : username,
	    j_password : password
	}, true);
	$http.post('/security-check', data, config).success(function(data, status, headers, config) {
	    if (data === 'success') {
		$rootScope.$broadcast('GeneralError', {
		    removeName : 'LoginError'
		});
		userChanged();
	    } else {
		$rootScope.$broadcast('GeneralError', {
		    name : 'LoginError',
		    message : 'Innlogging feilet for bruker ' +username
		});
	    }
	}).error(errorHandler);
    };

    function errorHandler(args) {
	$rootScope.$broadcast('GeneralError', {
	    name : AUTH_EVENT,
	    httpError : args
	});
    }

    this.logout = function() {
	$http.get('/logout').error(errorHandler);
	userChanged();
	$location.url('order_list');
    };

    return {
	current : this.current,
	sudo : this.su,
	authenticated : this.authenticated,
	login : this.login,
	logout : this.logout,
    }
} ];
