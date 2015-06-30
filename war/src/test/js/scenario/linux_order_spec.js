'use strict';

var OrderFormPage = require('../pages/orderform_page.js');
var LoginPartials = require('../pages/login_partials');


describe('Basta linux order', function () {
    var loginPage= new LoginPartials();
    var form= new OrderFormPage() 
    

    it('can be submitted', function () {
	form.get("/#/linux_order");
	loginPage.login("user", "user");
	
	form.setDescription("Tester med protractor")
        form.submit();
       
        expect(browser.getCurrentUrl()).toContain('order_details');
    });

   
    
});