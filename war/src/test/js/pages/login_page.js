'use strict';

var LoginPage = function() {

    this.isLoggedIn = function() {
	return element(by.binding('ctrl.user.displayName')).isDisplayed();
    }

    this.currentUser = function(){
	return element(by.binding('ctrl.user.displayName')).getText();
    }

    this.visit = function() {
	browser.get('http://localhost:1337/#/order_list');
    };
    this.setCredentials = function(username, password) {
	element(by.id('login_link')).click();
	element(by.binding('ctrl.userForm.username')).sendKeys(username);
	element(by.binding('ctrl.userForm.password')).sendKeys(password);
    };

    this.login = function(username, password) {
	element(by.id('login_link')).click();
	element(by.id('login_username')).sendKeys(username);
	element(by.id('login_password')).sendKeys(password);
	element(by.id('loginSubmit')).click();
    };

    this.logout = function() {
	element(by.id('logout_link')).click();
    };

};

module.exports = LoginPage;