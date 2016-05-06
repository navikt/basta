'use strict';
var OrderDetailsPage = require('./order_details_page.js');
var PageUtils = require('./page_utils');

function BigIPOrderPage() {
    browser.get("/#/bigip_order");
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
        browser.driver.wait(protractor.until.elementIsVisible(tag), 5000, "VirtualServer is not visible after 5 sec ")
        return PageUtils.clickUiSelect(tag, value);
    }
    , setContextRoot: function (value) {
        var tag = this.form.element(by.id('contextRoot'));
        return tag.element(by.tagName('input')).sendKeys(value);
    },

    setDns: function (value) {
    var tag = this.form.element(by.id('dns'));
    return tag.element(by.tagName('input')).sendKeys(value);
    },


hasVirtualServerMissingValidationError: function () {
        return this.form.element(by.id('virtualserverMissing')).isDisplayed();
    },

    hasLoadbalancerresourceMissingValidationError: function () {
        return this.form.element(by.id('loadbalancerresourceMissing')).isDisplayed();
    },


    submit: function () {
        var submitButton = this.form.element(by.id('submitOrder'))
        return submitButton.click().then(function () {
            return new OrderDetailsPage();
        });
    }
};

module.exports = BigIPOrderPage;
