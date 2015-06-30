'use strict';

var LoginPage = require('../pages/login_partials');
var MenuPartials = require('../pages/menu_partials');
var testConfig = require('../conf.js').config;

describe('Basta login', function () {
   var page= new LoginPage();
   var menu= new MenuPartials();

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
	page.login('superuser', 'superuser');
	expect(page.currentUser()).toEqual('superuser');
    });
    
    it('unauthorised user has access to orderlist', function () {
	page.logout();
	expect(page.isLoggedIn()).toBeFalsy();
	expect(menu.count()).toEqual(1);
    });
    
    it('user has access to orderlist and create menu items', function () {
	page.login('user', 'user');
	expect(page.isLoggedIn()).toBeTruthy();
	expect(menu.count()).toEqual(2);
    });
    
    it('superuser has access to orderlist, create, operations and notification menu items', function () {
	page.login('superuser', 'superuser');
	expect(page.isLoggedIn()).toBeTruthy();
	expect(menu.count()).toEqual(4);
    });
    
    

});