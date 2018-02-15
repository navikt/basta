'use strict';

var NodeOrderPage = require('../pages/node_order_page');
var loginPage = require('../pages/login_partials');

describe('Basta Jboss order', function() {

	beforeEach(function() {
		loginPage.login("user", "user");
	});

	afterEach(function () {
		loginPage.logout()
	})

	it('should create jboss node ok', function() {
		var orderPage = new NodeOrderPage("/#/jboss_order");
		orderPage.setEnvironment('cd-u1');
		orderPage.setApplication('fasit');
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Vm | Jboss |');
		});
	});
});