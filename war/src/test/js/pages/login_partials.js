'use strict';

// private

function doLogout(logoutButton) {
	return logoutButton.isDisplayed().then(function(loggedIn) {
		if (loggedIn) {
			// console.log("logging out");
			return logoutButton.click();
			// triks for å unngå problemer med loginting som ikke vises enda
//			return browser.driver.wait(protractor.until.elementIsNotVisible(logoutButton), 10000, "LogoutButton is still visible 10 sec after logging out");
		}
	});

}

// public


var Login=function() {
	this.loginContainer = element(by.id('loginContainer'));
	this.logoutButton = this.loginContainer.element(by.id('logout_link'));
	this.userNameLabel = this.loginContainer.element(by.binding('ctrl.user.displayName'));
}

Login.prototype = {
	isLoggedIn : function() {
		return this.logoutButton.isDisplayed();
	},

	currentUser : function() {
		return this.userNameLabel.getText();
	},

	login : function(username, password) {
		var logoutButton = this.logoutButton;
		var loginContainer = this.loginContainer;
		this.currentUser().then(function(currentUser) {
			if (currentUser !== username) {
				return doLogout(logoutButton)
				.then(function doLogin(){
					var loginButton = loginContainer.element(by.id('login_link'));
					return loginButton.click().then(function() {
						var login_username = loginContainer.element(by.id('login_username'));
						login_username.clear();
						login_username.sendKeys(username);

						var login_password = loginContainer.element(by.id('login_password'))
						login_password.clear();
						login_password.sendKeys(password);
						var loginSubmit = loginContainer.element(by.id('loginSubmit'));
						return loginSubmit.click();
						
					}).then(function() {
//						return browser.driver.wait(protractor.until.elementIsNotVisible(loginSubmit), 10000, "LoginSubmitbutton is still visible 10sec after logging in");
//						console.log("log in as ", username);
					});
				});
			} else {
//				console.log("Already logged in as", username);
			}
		});
	},

	logout : function() {
		return doLogout(this.logoutButton);
	},

};

module.exports = new Login();
