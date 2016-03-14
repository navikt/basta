'use strict';
var OrderDetailsPage = require('../pages/order_details_page.js');
var PageUtils = require('../pages/page_utils');

var OrderFormPage = function() {

	this.form = element(by.tagName('orderform'));

	this.get = function(url) {
		return browser.get(url);
	}

	this.setEnvironment = function(value) {
		var tag = this.form.element(by.tagName('orderform-environments'));
		return PageUtils.clickUiSelect(tag, value);
	}

	this.setApplication = function(value) {
		var tag = this.form.element(by.tagName('orderform-applications'));
		return PageUtils.clickUiSelect(tag, value);
	}

	this.setQueueName = function(value) {
		var tag = this.form.element(by.id('queueName'));
		return  tag.element(by.tagName('input')).sendKeys(value);
	}

	this.setQueueMananger = function(value) {
		var tag = this.form.element(by.tagName('orderform-queue-managers'));
		return PageUtils.clickUiSelect(tag, value);
	}


	this.submit = function() {
		var submitButton = this.form.element(by.id('submitOrder'))
		return submitButton.click().then(function() {
			return new OrderDetailsPage();
		});
	}

};

module.exports = OrderFormPage;