'use strict';

var OrderDetailsPage = function () {
//    browser.get('/#/order_details/' + id);
    
    var requestTabs= element(by.id('requestContentTabs'));
    
    this.pageHeader= function(){
	 return element(by.css('.page-header')).getText();
    }
    
    this.requestForm= function(){
	return element(by.id('form')).all(by.tagName('tr'));
    }
    
    
    
};

module.exports = OrderDetailsPage;