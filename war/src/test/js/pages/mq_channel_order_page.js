'use strict';
var OrderDetailsPage = require('./order_details_page.js');
var PageUtils = require('./page_utils');

function MqOrderPage() {
	browser.get("/#/mq_channel_order");
	this.form = element(by.tagName('orderform'));
}

MqOrderPage.prototype={
	setEnvironment : function(value) {
		var tag = this.form.element(by.tagName('orderform-environments'));
		return PageUtils.clickUiSelect(tag, value);
	},

	setApplication : function(value) {
		var tag = this.form.element(by.tagName('orderform-applications'));
		return PageUtils.clickUiSelect(tag, value);
	},

	hasValidationError : function(){
		return this.form.element(by.id('validationError')).isDisplayed();
	},

	setQueueMananger : function(value) {
		var tag = this.form.element(by.tagName('orderform-queue-managers'));
		browser.driver.wait(protractor.until.elementIsVisible(tag), 5000, "QueueMananger is not visible after 5 sec ")
		return PageUtils.clickUiSelect(tag, value);
	},


	submit : function() {
		var submitButton = this.form.element(by.id('submitOrder'))
		return submitButton.click().then(function() {
			return new OrderDetailsPage();
		});
	}
};

module.exports = MqOrderPage;
