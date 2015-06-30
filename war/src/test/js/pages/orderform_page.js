'use strict';

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
	var tag= this.form.element(by.tagName('orderform-applications'));
	return clickUiSelect(tag, value);
    }
    
    this.setDescription= function(value){
	var tag= this.form.element(by.id('description'));
	return tag.element(by.tagName('input')).sendKeys(value);
    }

    function clickUiSelect(tag, value) {
	var selectButton = tag.element(by.css('.ui-select-container'));
	var selectInput = tag.element(by.css('.ui-select-search'));

	// click to open select
	selectButton.click();
	// type some text
	selectInput.sendKeys(value);
	// select first element
	 element.all(by.css('.ui-select-choices-row')).first().click();
    }

    this.submit = function() {
	var submitButton= this.form.element(by.id('submitOrder'))
//	console.log(submitButton.getText())
	return submitButton.click();
    }

};

module.exports = OrderFormPage;