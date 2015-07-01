'use strict';

var OrderDetailsPage = require('../pages/order_details_page.js');

var OrderListPage = function () {
    browser.get('/#/order_list');
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
        var firstOrderLink = this.firstOrderLink();
        return firstOrderLink.getText().then(function (id){
              return firstOrderLink.click().then(function () {
                  return new OrderDetailsPage( id);
            });
        });
    }}
});

module.exports = OrderListPage;