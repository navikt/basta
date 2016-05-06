'use strict';

var BigIPOrderPage = require('../pages/bigip_order_page');
var loginPage = require('../pages/login_partials');

describe('Basta BigIP order', function() {

	beforeEach(function() {
		loginPage.login("user", "user");
	});

	it('should create big ip config ok', function() {
		var orderPage = new BigIPOrderPage("/#/bigip_order");
		orderPage.setEnvironment('cd-u1');
		orderPage.setApplication('fasit');
		orderPage.setVirtualServer('vs_name_1')
		orderPage.setContextRoot("protractor, context, roots")
		orderPage.setDns("http://an.optional.dns.for.good.measure");
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Bigip | Big Ip Config');
		});
	});


});