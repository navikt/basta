'use strict';

var OrderListPage = require('../pages/order_list_page.js');

describe('Basta order list', function () {
    var page;

    beforeEach(function () {
        page = new OrderListPage();
    })

    it('has title basta', function () {
        expect(page.title).toEqual('basta');
    });

    it('has some orders', function () {
        expect(page.orderlist.count()).toBeGreaterThan(0);
    });

    it('lets you click on the first order, and then go to details view for the corresponding order', function () {
        var orderId = page.firstOrderId;
        page.goToFirstOrderDetails.then(function (orderDetails) {
            expect(orderDetails.pageHeader()).not.toContain("does not exist");
        });
    });
    
});