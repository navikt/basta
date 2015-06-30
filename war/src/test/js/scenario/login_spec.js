'use strict';

var LoginPage = require('../pages/login_page');
var testConfig = require('../conf.js').config;

describe('Basta login', function () {
   var page= new LoginPage();

    beforeEach(function () {
	browser.get(testConfig.baseUrl);
    });
    
    it('is possible to log in as user', function () {
     	page.login('user', 'user');
	expect(page.isLoggedIn()).toBeTruthy();
	expect(page.currentUser()).toEqual('user');
	page.logout()
    });
    
    it('is possible to log out', function () {
     	page.login('user', 'user');
	expect(page.isLoggedIn()).toBeTruthy();
	page.logout();
	expect(page.isLoggedIn()).toBeFalsy();
    });
    
    it('is possible to log in as user and then login as superuser', function () {
     	page.login('user', 'user');
	expect(page.isLoggedIn()).toBeTruthy();
	expect(page.currentUser()).toEqual('user');
	page.logout();
	page.login('superuser', 'superuser');
	expect(page.currentUser()).toEqual('superuser');
    });
    
    

});