'use strict';

var LoginPartials = function() {
    var loginButton = element(by.id('login_link'));
    var logoutButton = element(by.id('logout_link'));
    var userName = element(by.binding('ctrl.user.displayName'));

    this.isLoggedIn = function() {
	return logoutButton.isDisplayed();
    }

    this.currentUser = function() {
	return userName.getText();
    }


    function doLogin(username, password) {
	console.log("log in as ", username);
	loginButton.click();
	element(by.id('login_username')).sendKeys(username);
	element(by.id('login_password')).sendKeys(password);
	return element(by.id('loginSubmit')).click();
    }

    function logoutIfLoggedIn() {
	return logoutButton.isDisplayed().then(function(loggedIn) {
	    if (loggedIn) {
		return element(by.id('logout_link')).click().then(function(){
		    console.log("User is logged in, Forcing a logout");
		});
	    }
	});

    }

    this.login = function(username, password) {
	return logoutIfLoggedIn()
	.then(doLogin(username, password));
    };

    this.logout = function() {
	return this.isLoggedIn().then(function(loggedIn) {
	    if (loggedIn) {
		console.log("logging out");
		return logoutButton.click();
	    }
	});
    };

};

module.exports = LoginPartials;