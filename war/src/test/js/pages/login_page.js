'use strict';

var LoginPage = function() {
    this.userName = element(by.binding('ctrl.user.displayName'));

    this.isLoggedIn = function() {
	return this.userName.isDisplayed();
    }

    this.currentUser = function() {
	return this.userName.getText();
    }

    this.login = function(username, password) {
	return this.isLoggedIn().then(function(loggedIn) {
	    if (loggedIn) {
		element(by.id('logout_link')).click().then(function() {
		    console.log("User is logged in, Forcing a logout");
		});
	    }
	}).then(function() {
	    browser.waitForAngular();
	    console.log("log in as ", username);
	    element(by.id('login_link')).click();
	    element(by.id('login_username')).sendKeys(username);
	    element(by.id('login_password')).sendKeys(password);
	    return element(by.id('loginSubmit')).click();
	})

    };

    this.logout = function() {
	return this.isLoggedIn().then(function(loggedIn) {
	    if (loggedIn) {
		console.log('logging out')
		return element(by.id('logout_link')).click();
	    }
	});
    };

};

module.exports = LoginPage;