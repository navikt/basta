'use strict';

var clickUiSelect = function(tag, value) {
	var selectContainer = tag.element(by.css('.ui-select-container'));
	var selectInput = tag.element(by.css('.ui-select-search'));

	// click to open select
	selectContainer.click()
	// type some text
	browser.driver.wait(protractor.until.elementIsVisible(selectInput), 5000, "Select input is not visible after 5 sec ")
	selectInput.clear();
	selectInput.sendKeys(value);
	// select first element
	return element.all(by.css('.ui-select-choices-row')).first().click();
}

module.exports.clickUiSelect = clickUiSelect;
