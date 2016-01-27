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
	    var login_username=element(by.id('login_username'));
	    login_username.clear();
	    login_username.sendKeys(username);
	    
	    var login_password= element(by.id('login_password'))
	    login_password.clear();
	    login_password.sendKeys(password);
	    element(by.id('loginSubmit')).click();
	    return browser.driver.wait(protractor.until.elementIsVisible(userName), 5000, "Username is not visible 5 sec after logging in");
	}).then(function() {
//	    console.log("log in as ", username);
	});
    }

    function doLogout() {
	return logoutButton.isDisplayed().then(function(loggedIn) {
	    if (loggedIn) {
//		console.log("logging out");
		logoutButton.click();
		// triks for å unngå problemer med loginting som ikke vises enda
		return browser.driver.wait(protractor.until.elementIsNotVisible(logoutButton), 10000, "LogoutButton is still visible 10 sec after logging out");
	    }
	});

    }

    this.login = function(username, password) {
	this.currentUser().then(function(currentUser) {
	    if (currentUser !== username) {
		return doLogout()
		.then(doLogin(username, password));
	    } else {
//		console.log("Allready logged in as", username);
	    }
	});
    };

    this.logout = function() {
	return doLogout();
    };

};

module.exports = LoginPartials;