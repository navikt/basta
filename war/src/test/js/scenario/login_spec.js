'use strict';

var LoginPartials = require('../pages/login_partials');
var MenuPartials = require('../pages/menu_partials');
var testConfig = require('../conf.js').config;

describe('Basta login', function () {
   var loginPage= new LoginPartials();
   var menu= new MenuPartials();

    beforeEach(function () {
	browser.get(testConfig.baseUrl);
    });
    
    it('is possible to log in as user', function () {
     	loginPage.login('user', 'user');
	expect(loginPage.isLoggedIn()).toBeTruthy();
	expect(loginPage.currentUser()).toEqual('user');
	loginPage.logout()
    });
    
    it('is possible to log out', function () {
     	loginPage.login('user', 'user');
	expect(loginPage.isLoggedIn()).toBeTruthy();
	loginPage.logout();
	expect(loginPage.isLoggedIn()).toBeFalsy();
    });
    
    it('is possible to log in as user and then login as superuser', function () {
     	loginPage.login('user', 'user');
	expect(loginPage.isLoggedIn()).toBeTruthy();
	expect(loginPage.currentUser()).toEqual('user');
	loginPage.login('superuser', 'superuser');
	expect(loginPage.currentUser()).toEqual('superuser');
    });
    
    it('unauthorised user has access to orderlist', function () {
	loginPage.logout();
	expect(loginPage.isLoggedIn()).toBeFalsy();
	expect(menu.count()).toEqual(1);
    });
    
    it('user has access to orderlist and create menu items', function () {
	loginPage.login('user', 'user');
	expect(loginPage.isLoggedIn()).toBeTruthy();
	expect(menu.count()).toEqual(2);
    });
    
    it('superuser has access to orderlist, create, operations and notification menu items', function () {
	loginPage.login('superuser', 'superuser');
	expect(loginPage.isLoggedIn()).toBeTruthy();
	expect(menu.count()).toEqual(4);
    });
    
    

});