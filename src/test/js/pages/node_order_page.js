'use strict';
var OrderDetailsPage = require('./order_details_page.js');
var PageUtils = require('./page_utils');

function NodeOrderPage(url) {
	browser.get(url);
	this.form = element(by.tagName('orderform'));
}

NodeOrderPage.prototype={
	setEnvironment : function(value) {
		var tag = this.form.element(by.tagName('orderform-environments'));
		return PageUtils.clickUiSelect(tag, value);
	},

	setApplication : function(value) {
		var tag = this.form.element(by.tagName('orderform-applications'));
		return PageUtils.clickUiSelect(tag, value);
	},

	submit : function() {
		var submitButton = this.form.element(by.id('submitOrder'))
		return submitButton.click().then(function() {
			return new OrderDetailsPage();
		});
	}
};

module.exports = NodeOrderPage;
