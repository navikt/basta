'use strict';

var OrderFormPage = require('../pages/linux_orderform_page');
var loginPage = require('../pages/login_partials');

describe('Basta linux order', function() {
	var form = new OrderFormPage()

	it('create', function() {
		form.get("/#/linux_order");
		loginPage.login("user", "user");

		form.setDescription("Tester med protractor")
		form.submit();

		expect(browser.getCurrentUrl()).toContain('order_details');
	});

});