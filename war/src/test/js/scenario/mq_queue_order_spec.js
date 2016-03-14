'use strict';

var MqOrderPage = require('../pages/mq_queue_order_page');
var OperationPage = require('../pages/mq_queue_operation_page');
var LoginPartials = require('../pages/login_partials');

describe('Basta mq queue', function() {
	var loginPage = new LoginPartials();
	var orderPage = new MqOrderPage();
	var operations = new OperationPage();

	beforeEach(function() {
		if(!loginPage.isLoggedIn()){
			loginPage.login("user", "user");
		}
	});

	it('should create mq queue ok', function() {
		
		orderPage.get("/#/mq_queue_order");
		orderPage.setEnvironment('cd-u1')
		orderPage.setApplication('fasit');
		orderPage.setQueueName("E2EQueue");
		orderPage.setQueueMananger("CLIENT0");
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create Mq of type Queue');
		});
	});

	it('stop mq queue', function() {
		operations.get("/#/operations_queue");
		operations.setQueueMananger("CLIENT0");
		operations.setQueueName("U1_MOCK_QUEUE2");
		operations.stop().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Stop Mq');
		});
	});
	it('start mq queue', function() {
		operations.get("/#/operations_queue");
		operations.setQueueMananger("CLIENT0");
		operations.setQueueName("U1_MOCK_QUEUE2");
		operations.start().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Start Mq');
		});
	});
	it('delete mq queue', function() {
		operations.get("/#/operations_queue");
		operations.setQueueMananger("CLIENT0");
		operations.setQueueName("U1_MOCK_QUEUE2");
		operations.remove().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Delete Mq');
		});
	});

});