'use strict';

var MenuPartials = function() {
    this.menu = element(by.id('side_menu')).all(by.tagName('li')).filter(function(el, index){
	return el.isDisplayed();
    });
    
    this.count = function(){
	return this.menu.count();
    }

   
};

module.exports = MenuPartials;