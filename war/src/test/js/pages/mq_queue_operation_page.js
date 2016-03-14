'use strict';
var OrderDetailsPage = require('../pages/order_details_page.js');
var PageUtils = require('../pages/page_utils');

var OrderFormPage = function() {

	this.form = element(by.tagName('orderform'));

	this.get = function(url) {
		return browser.get(url);
	}

	this.setQueueName = function(value) {
		var tag = this.form.element(by.id('queueName'));
		return PageUtils.clickUiSelect(tag, value);
	}

	this.setQueueMananger = function(value) {
		var tag = this.form.element(by.id('queueManager'));
		return PageUtils.clickUiSelect(tag, value);
	}

	function click(form, id){
		var button = form.element(by.tagName('operation-buttons')).element(by.id(id));
		return button.click().then(function() {
			return new OrderDetailsPage();
		});
	}

	this.stop = function() {
		return click(this.form, 'stopBtn');
	}
	this.start = function() {
		return click(this.form, 'startBtn');
	}
	this.remove = function() {
		return click(this.form, 'deleteBtn');
	}

};

module.exports = OrderFormPage;