'use strict';

var OrderDetailsPage = function (baseUrl, id) {
    browser.get(baseUrl + '/#/order_details/' + id);
};

OrderDetailsPage.prototype = Object.create({}, {
    pageHeader: { get: function () {
        return element(by.css('.page-header')).getText();
    }},
    results:{ get: function(){
	
    } }
    

});

module.exports = OrderDetailsPage;