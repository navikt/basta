'use strict';
var OrderDetailsPage = require('./order_details_page.js');
var PageUtils = require('./page_utils');

function ContainerLinuxOrderFormPage() {
	browser.get("/#/containerlinux_order");
	this.form = element(by.tagName('orderform'));
}

ContainerLinuxOrderFormPage.prototype = {

	setDescription : function(value) {
		var tag = this.form.element(by.id('description'));
		return tag.element(by.tagName('input')).sendKeys(value);
	},

	submit : function() {
		var submitButton = this.form.element(by.id('submitOrder'))
		return submitButton.click().then(function() {
			return new OrderDetailsPage();
		});
	}

};

module.exports = ContainerLinuxOrderFormPage;