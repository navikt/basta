'use strict';

var OrderFormPage = require('../pages/orderform_page.js');
var LoginPartials = require('../pages/login_partials');


describe('Basta boss order', function () {
    var loginPage= new LoginPartials();
    var form= new OrderFormPage() 
    

    it('can be submitted', function () {
	form.get("/#/jboss_order");
	loginPage.login("user", "user");
	
	form.setEnvironment('cd-u1')
	form.setApplication('photon');
        form.submit();
        
        browser.driver.sleep(10);
	    browser.waitForAngular();
//        expect(browser.getCurrentUrl()).toContain('order_details');
    });

   
    
});