'use strict';
var OrderDetailsPage = require('../pages/order_details_page.js');

var OrderFormPage = function() {

	this.form = element(by.tagName('orderform'));

	this.get = function(url) {
		return browser.get(url);
	}

	this.setEnvironment = function(value) {
		var tag = this.form.element(by.tagName('orderform-environments'));
		return clickUiSelect(tag, value);
	}

	this.setApplication = function(value) {
		var tag = this.form.element(by.tagName('orderform-applications'));
		return clickUiSelect(tag, value);
	}

	this.setQueueName = function(value) {
		var tag = this.form.element(by.id('queueName'));
		return  tag.element(by.tagName('input')).sendKeys(value);
	}

	this.setQueueMananger = function(value) {
		var tag = this.form.element(by.tagName('orderform-queue-manangers'));
		return clickUiSelect(tag, value);
	}

	function clickUiSelect(tag, value) {
		var selectButton = tag.element(by.css('.ui-select-container'));
		var selectInput = tag.element(by.css('.ui-select-search'));

		// click to open select
		selectButton.click();
		// type some text
		selectInput.clear();
		selectInput.sendKeys(value);
		// select first element
		element.all(by.css('.ui-select-choices-row')).first().click();
	}

	this.submit = function() {
		var submitButton = this.form.element(by.id('submitOrder'))
		return submitButton.click().then(function() {
			return new OrderDetailsPage();
		});
	}

};

module.exports = OrderFormPage;