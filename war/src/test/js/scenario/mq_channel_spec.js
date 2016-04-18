'use strict';

var MqOrderPage = require('../pages/mq_channel_order_page');
var OperationPage = require('../pages/mq_channel_operation_page');
var loginPage = require('../pages/login_partials');

beforeEach(function() {
	loginPage.login("user", "user");
});

describe('Basta mq channel order', function() {
	it('should create mq channel ok', function() {
		var orderPage = new MqOrderPage();
		orderPage.setEnvironment('cd-u1');
		orderPage.setApplication('fasit');
		orderPage.setQueueMananger("CLIENT0");
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Mq | Channel |');
		});
	});

	it('should create mq channel with valdiation error', function() {
		var orderPage = new MqOrderPage();
		orderPage.setEnvironment('u3');
		orderPage.setApplication('app1');
		orderPage.setQueueMananger("CLIENT0");
		orderPage.submit().then(function(orderDetails) {
			expect(orderPage.hasValidationError());
		});
	});
});

describe('Basta mq channel operations', function() {
	it('stop mq channel', function() {
		var operations = new OperationPage();
		operations.setQueueMananger("CLIENT0");
		operations.setChannelName("U1_MYAPP");
		operations.stop().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Stop | Mq');
		});
	});
	it('start mq channel', function() {
		var operations = new OperationPage();
		operations.setQueueMananger("CLIENT0");
		operations.setChannelName("U1_MYAPP");
		operations.start().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Start | Mq');
		});
	});
	it('delete mq channel', function() {
		var operations = new OperationPage();
		operations.setQueueMananger("CLIENT0");
		operations.setChannelName("U1_MYAPP");
		operations.remove().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Delete | Mq');
		});
	});

});
