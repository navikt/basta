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
	return loginButton.click().then(function() {
	    element(by.id('login_username')).sendKeys(username);
	    element(by.id('login_password')).sendKeys(password);
	    return element(by.id('loginSubmit')).click();
	}).then(function() {
	    console.log("log in as ", username);
	});
    }

    function logoutIfLoggedIn() {
	var logoutlink = logoutButton;
	return logoutlink.isDisplayed().then(function(loggedIn) {
	    if (loggedIn) {
		console.log("Another user is logged in, Forcing a logout");
		element(by.id('logout_link')).click();
		// triks for å unngå problemer med loginting som ikke vises enda
		return browser.driver.wait(protractor.until.elementIsNotVisible(logoutlink));
		
	    }
	});

    }

    this.login = function(username, password) {
	this.currentUser().then(function(currentUser) {
	    if (currentUser !== username) {
		return logoutIfLoggedIn()
		.then(doLogin(username, password));
	    } else {
		console.log("Allready logged in as", username);
	    }
	});
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