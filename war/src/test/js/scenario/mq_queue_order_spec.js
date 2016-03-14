'use strict';

var OrderFormPage = require('../pages/mq_queue_order_page');
var OperationPage = require('../pages/mq_queue_operation_page');
var LoginPartials = require('../pages/login_partials');

describe('Basta mq queue', function() {
	var loginPage = new LoginPartials();

	it('should create mq queue ok', function() {
		var form = new OrderFormPage()
		form.get("/#/mq_queue_order");
		loginPage.login("user", "user");
		form.setEnvironment('cd-u1')
		form.setApplication('fasit');
		form.setQueueName("E2EQueue");
		form.setQueueMananger("CLIENT0");
		form.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create Mq of type Queue');
		});
	});
	
	it('stop mq queue', function() {
		var form = new OperationPage()
		form.get("/#/operations_queue");
		loginPage.login("user", "user");
		form.setQueueMananger("CLIENT0");
		form.setQueueName("U1_MOCK_QUEUE2");
		form.stop().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Stop Mq');
		});
	});
	it('start mq queue', function() {
		var form = new OperationPage()
		form.get("/#/operations_queue");
		loginPage.login("user", "user");
		form.setQueueMananger("CLIENT0");
		form.setQueueName("U1_MOCK_QUEUE2");
		form.start().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Start Mq');
		});
	});
	it('delete mq queue', function() {
		var form = new OperationPage()
		form.get("/#/operations_queue");
		loginPage.login("user", "user");
		form.setQueueMananger("CLIENT0");
		form.setQueueName("U1_MOCK_QUEUE2");
		form.remove().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Delete Mq');
		});
	});

});