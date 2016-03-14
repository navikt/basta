'use strict';
var OrderDetailsPage = require('./order_details_page.js');
var PageUtils = require('./page_utils');

var MqOrderPage = function() {
	var form = element(by.tagName('orderform'));

	this.get = function(url) {
		 browser.get(url);
	}

	this.setEnvironment = function(value) {
		var tag = form.element(by.tagName('orderform-environments'));
		return PageUtils.clickUiSelect(tag, value);
	}

	this.setApplication = function(value) {
		var tag = form.element(by.tagName('orderform-applications'));
		return PageUtils.clickUiSelect(tag, value);
	}

	this.setQueueName = function(value) {
		var tag = form.element(by.id('queueName'));
		return  tag.element(by.tagName('input')).sendKeys(value);
	}

	this.setQueueMananger = function(value) {
		var tag = form.element(by.tagName('orderform-queue-managers'));
		return PageUtils.clickUiSelect(tag, value);
	}


	this.submit = function() {
		var submitButton = form.element(by.id('submitOrder'))
		return submitButton.click().then(function() {
			return new OrderDetailsPage();
		});
	}

};

module.exports = MqOrderPage;