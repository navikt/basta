'use strict';

var Login = function () {
}

Login.prototype = {
    isLoggedIn: function () {
        var loginContainer = element(by.id('loginContainer'));
        var logoutButton = loginContainer.element(by.id('logout_link'));
        return logoutButton.isDisplayed()
    },

    currentUser: function () {
        var loginContainer = element(by.id('loginContainer'));
        var userNameLabel = loginContainer.element(by.id('currentUser'));
        return userNameLabel.getText()
    },

    login: function (username, password) {
        var loginContainer = element(by.id('loginContainer'));
        var loginButton = loginContainer.element(by.id('login_link'));
        loginButton.click().then(function () {
            var login_username = loginContainer.element(by.id('login_username'));
            login_username.clear()
            login_username.sendKeys(username)

            var login_password = loginContainer.element(by.id('login_password'))
            login_password.clear()
            login_password.sendKeys(password)

            var loginSubmit = loginContainer.element(by.id('loginSubmit'));
            loginSubmit.click().then(function () {
                browser.wait(protractor.ExpectedConditions.invisibilityOf(loginSubmit))
            })
        })
    },
    logout: function () {
        var loginContainer = element(by.id('loginContainer'));
        var logoutButton = loginContainer.element(by.id('logout_link'));

        logoutButton.click().then(function () {
            browser.wait(protractor.ExpectedConditions.invisibilityOf(logoutButton))
        })
    }
}

module.exports = new Login()