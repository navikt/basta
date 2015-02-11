'use strict';

var OrderDetailsPage = function (baseUrl, id) {
    browser.get(baseUrl + '/#/order_details/' + id);
};

OrderDetailsPage.prototype = Object.create({}, {
    pageHeader: { get: function () {
        return element(by.binding('orderDetails.id')).getText();
    }}
});

module.exports = OrderDetailsPage;