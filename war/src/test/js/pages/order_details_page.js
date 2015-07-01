'use strict';

var OrderDetailsPage = function (id) {
    browser.get('/#/order_details/' + id);
};

OrderDetailsPage.prototype = Object.create({}, {
    pageHeader: { get: function () {
        return element(by.css('.page-header')).getText();
    }},
    results:{ get: function(){
	
    } }
    

});

module.exports = OrderDetailsPage;