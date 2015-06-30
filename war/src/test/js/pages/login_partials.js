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

    function logoutIfNotLoggedIn() {
	return logoutButton.isDisplayed().then(function(loggedIn) {
	    if (loggedIn) {
		return element(by.id('logout_link')).click().then(function() {
		    console.log("User is logged in, Forcing a logout");
		    return ;
		});
	    }
	});
    }

    this.login = function(username, password) {
	logoutIfNotLoggedIn();
//	browser.waitForAngular();
	console.log("log in as ", username);
	loginButton.click();
	element(by.id('login_username')).sendKeys(username);
	element(by.id('login_password')).sendKeys(password);
	return element(by.id('loginSubmit')).click();

    };

    this.logout = function() {
	return this.isLoggedIn().then(function(loggedIn) {
	    if (loggedIn) {
		return logoutButton.click().then(function() {
		    console.log("logging out");
		    return ;
		});
	    }
	});
    };

};

module.exports = LoginPartials;