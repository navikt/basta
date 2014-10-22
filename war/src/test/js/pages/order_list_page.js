'use strict';

var OrderDetailsPage = require('../pages/order_details_page.js');

var OrderListPage = function (baseUrl) {
    this.baseUrl = baseUrl;
    browser.get(baseUrl+'/#/order_list');
};

OrderListPage.prototype = Object.create({}, {

    title:{ get: function() {
        return browser.getTitle();
    }},
    orderlist: { get: function () {
        return element.all(by.repeater('order in orders'));
    }},
    firstOrderLink: { value : function(){
       return this.orderlist
           .first()
           .all(by.tagName('td'))
           .first()
           .element(by.tagName('a'));
    }},
    firstOrderId: { get: function(){
        return this.firstOrderLink().getText().then(function (text) {
            return text;
        });
    }},
    goToFirstOrderDetails: { get: function(){
        var baseUrl = this.baseUrl;
        var firstOrderLink = this.firstOrderLink();
        return firstOrderLink.getText().then(function (text){
              return firstOrderLink.click().then(function () {
                  return new OrderDetailsPage(baseUrl, text);
            });
        });
    }}
});

module.exports = OrderListPage;