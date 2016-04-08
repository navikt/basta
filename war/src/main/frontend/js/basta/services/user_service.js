'use strict';

module.exports = [ '$http', 'errorService', '$rootScope','$interval', '$location', function($http, errorService, $rootScope, $interval, $location) {

    var AUTH_EVENT= 'Autentiseringsfeil';
    var currentUser=  {name:'unauthenticated', displayName:'unauthenticated', environmentClasses:[]};
    init();
    
    var service = {
	login : login,
	logout : logout,
	current : getCurrentUser,
	isSuperuser: isSuperuser,
	isAuthenticated: isAuthenticated,
	hasEnvironmentClassAccess: hasEnvironmentClassAccess,
	onchange : onchange
	
	
    }
    return service;
   
    function init(){
	updateCurrentUser();
	$interval(isUserChanged, 10000);
    }
    
    function onchange(callback){
	callback();
	$rootScope.$on("UserChanged", callback)
    }
    
    function getCurrentUser(){
	return currentUser;
    }
    
    function isSuperuser() {
	return (isAuthenticated() && currentUser.superUser);
    }
    
    function isAuthenticated() {
	return (!_.isUndefined(currentUser) && currentUser.authenticated);
    }
    
    function hasEnvironmentClassAccess(environmentClass) {
        if ( isAuthenticated()) {
            var classes = currentUser.environmentClasses;
            return classes.indexOf(environmentClass) > -1;
        }
        return false;
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
		// console.log("user updated", currentUser);
	    }
	}).then(function(){
	    $rootScope.$broadcast('UserChanged');
	} );
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

    }
    
    function login(username, password) {
	// console.log("logging in user", username);
	var config = {
	    headers : {
		'Content-Type' : 'application/x-www-form-urlencoded'
	    }
	};
	var data = $.param({
	    username : username,
	    password : password
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

    function logout() {
	$http.get('/logout').error(errorHandler);
	userChanged();
	$location.url('order_list');
    };

} ];
