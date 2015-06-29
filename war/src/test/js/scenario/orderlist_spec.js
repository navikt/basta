'use strict';

var OrderListPage = require('../pages/order_list_page.js');
var testConfig = require('../conf.js').config;

describe('Basta', function () {
    var page, detailsPage;

    beforeEach(function () {
        page = new OrderListPage(testConfig.baseUrl);
    });

    it('has title basta', function () {
        expect(page.title).toEqual('basta');
    });

    it('has some orders', function () {
        expect(page.orderlist.count()).toBeGreaterThan(0);
    });

    it('lets you click on the first order, and then go to details view for the corresponding order', function () {
        var orderId = page.firstOrderId;
        page.goToFirstOrderDetails.then(function (orderDetails) {
            expect(orderDetails.pageHeader).not.toContain("does not exist");
        });
    });
});