'use strict';
// private
var requestTabs = element(by.id('requestContentTabs'));

function OrderDetailsPage() {
}

OrderDetailsPage.prototype = {

	pageHeader : function() {
		return element(by.css('.page-header')).getText();
	},

	requestForm : function() {
		return element(by.id('form')).all(by.tagName('tr'));
	}

};

module.exports = OrderDetailsPage;