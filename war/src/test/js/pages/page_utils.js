'use strict';



var clickUiSelect =function(tag, value) {
	var selectButton = tag.element(by.css('.ui-select-container'));
	var selectInput = tag.element(by.css('.ui-select-search'));

	// click to open select
	selectButton.click();
	// type some text
	selectInput.clear();
	selectInput.sendKeys(value);
	// select first element
	element.all(by.css('.ui-select-choices-row')).first().click();
}

module.exports = clickUiSelect;