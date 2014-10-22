'use strict';

var OrderListPage = require('../pages/order_list_page.js');

describe('Basta', function () {
    var page, detailsPage;
    var baseUrl = 'https://basta.adeo.no';

    beforeEach(function () {
        page = new OrderListPage(baseUrl);
    });

    it('has title BASTA', function () {
        expect(page.title).toEqual('BASTA');
    });

    it('has some orders', function () {
        expect(page.orderlist.count()).toBeGreaterThan(0);
    });

    it('must be able to click on the first order, and then go to details view for the corresponding order', function () {
        var orderId = page.firstOrderId;
        page.goToFirstOrderDetails.then(function (orderDetails) {
            expect(orderDetails.pageHeader).toContain(orderId);
        });
    });
});