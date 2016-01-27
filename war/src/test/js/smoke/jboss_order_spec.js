'use strict';

var OrderFormPage = require('../pages/orderform_page.js');
var LoginPartials = require('../pages/login_partials');
var OrderDetailsPage = require('../pages/order_details_page');

describe('Basta jboss order', function() {
	var loginPage = new LoginPartials();
	var form = new OrderFormPage()
	var detailsPage;

	it('can be submitted', function() {
		form.get("/#/jboss_order");
		loginPage.login("user", "user");

		form.setEnvironment('cd-u1')
		form.setApplication('fasit');
		// form.setServerCount(1);
		form.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			console.log(orderDetails);
			expect(orderDetails.pageHeader()).toContain('Create Vm of type Jboss');
			expect(orderDetails.requestForm().count()).toBeGreaterThan(2);
		});

	});

});