'use strict';
var OrderDetailsPage = require('./order_details_page.js');
var PageUtils = require('./page_utils');

function BigIPOrderPage(url) {
    url = !url ? "/#/bigip_order" : url;
    browser.get(url);
    this.form = element(by.tagName('orderform'));
}

BigIPOrderPage.prototype = {
    setEnvironment: function (value) {
        var tag = this.form.element(by.tagName('orderform-environments'));
        return PageUtils.clickUiSelect(tag, value);
    },

    setApplication: function (value) {
        var tag = this.form.element(by.tagName('orderform-applications'));
        return PageUtils.clickUiSelect(tag, value);
    },

    setVirtualServer: function (value) {
        var tag = this.form.element(by.id('virtualServer'));
        browser.driver.wait(protractor.until.elementIsVisible(tag.getWebElement()), 5000, "VirtualServer is not visible after 5 sec ");
        return PageUtils.clickUiSelect(tag, value);
    },

    setMatchingType: function (value) {
        var matchingTypeBtn = this.form.element(by.id(value));
        console.log("btn", matchingTypeBtn);
        matchingTypeBtn.click()
    },

    setContextRoot: function (value) {
        var tag = this.form.element(by.id('contextRoot'));
        return tag.element(by.tagName('input')).sendKeys(value);
    },

    setHostname: function (value) {
        var tag = this.form.element(by.id('hostname'));
        return tag.element(by.tagName('input')).sendKeys(value);
    },

    submit: function () {
        var submitButton = this.form.element(by.id('submitOrder'));
        return submitButton.click().then(function () {
            return new OrderDetailsPage();
        });
    }
};

module.exports = BigIPOrderPage;