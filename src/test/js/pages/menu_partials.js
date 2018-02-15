'use strict';

function MenuPartials() {
	this.sideMenu = element(by.id('side_menu'));
};

MenuPartials.prototype.count = function() {
	// browser.driver.wait(protractor.until.elementIsVisible(this.sideMenu), 1000, "Menu is not visible");
	var sideMenu=this.sideMenu;
	return browser.sleep(500).then(function() {
		return sideMenu.all(by.tagName('li')).filter(function(el, index) {
			return el.isDisplayed();
		}).count();
	});
}

module.exports = MenuPartials;