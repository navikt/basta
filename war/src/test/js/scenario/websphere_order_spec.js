'use strict';

var NodeOrderPage = require('../pages/node_order_page');
var loginPage = require('../pages/login_partials');

describe('Basta websphere orders', function() {

	beforeEach(function() {
		loginPage.login("user", "user");
	});

	it('should create was node ok', function() {
		var orderPage = new NodeOrderPage("/#/was_node_order");
		orderPage.setEnvironment('cd-u1');
		orderPage.setApplication('fasit');
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Vm | Was Nodes |');
		});
	});

	it('should create was dmgr ok', function() {
		var orderPage = new NodeOrderPage("/#/was_dmgr_order");
		orderPage.setEnvironment('u3');
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Vm | Was Deployment Manager |');
		});
	});

	it('should create was liberty ok', function() {
		var orderPage = new NodeOrderPage("/#/liberty_order");
		orderPage.setEnvironment('u3');
		orderPage.setApplication('fasit');
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Vm | Liberty |');
		});
	});

	it('should create bpm dmgr ok', function() {
		var orderPage = new NodeOrderPage("/#/bpm_dmgr_order");
		orderPage.setEnvironment('u3');
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Vm | Bpm Deployment Manager |');
		});
	});

	it('should create bpm node ok', function() {
		var orderPage = new NodeOrderPage("/#/bpm_node_order");
		orderPage.setEnvironment('cd-u1');
		orderPage.submit().then(function(orderDetails) {
			expect(browser.getCurrentUrl()).toContain('order_details');
			expect(orderDetails.pageHeader()).toContain('Create | Vm | Bpm Nodes |');
		});
	});
});