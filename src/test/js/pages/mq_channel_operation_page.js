'use strict';
var OrderDetailsPage = require('./order_details_page');
var PageUtils = require('./page_utils');
// private

function click(form, id) {
	var button = form.element(by.tagName('operation-buttons')).element(by.id(id));
	return button.click().then(function() {
		return new OrderDetailsPage();
	});
}

// public
module.exports = MqOperationPage;

function MqOperationPage() {
	browser.get('/#/operations_channel');
	this.form = element(by.tagName('orderform'));
}

MqOperationPage.prototype = {

	setChannelName : function(value) {
		var tag = this.form.element(by.id('channelName'));
		return PageUtils.clickUiSelect(tag, value);
	},

	setQueueMananger : function(value) {
		var tag = this.form.element(by.id('queueManager'));
		return PageUtils.clickUiSelect(tag, value);
	},

	stop : function() {
		return click(this.form, 'stopBtn');
	},
	start : function() {
		return click(this.form, 'startBtn');
	},
	remove : function() {
		return click(this.form, 'deleteBtn');
	}

}