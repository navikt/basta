'use strict';

var LoginPage=require('../pages/login_page');

describe('Login', function () {
    var page;

    beforeEach(function () {
	page= new LoginPage();
    });

    //    it('is not logged in', function () {
//        expect(loginPage.isLoggedIn).toBeFalsy();
//    });
    
    
    it('is possible to log in ', function () {
	page.visit();
     	expect(page.isLoggedIn()).toBeFalsy();
//     	page.setCredentials('user', 'user');
     	page.login('user', 'user');
	expect(page.isLoggedIn()).toBeTruthy();
	expect(page.currentUser()).toEqual('user');
    });

});