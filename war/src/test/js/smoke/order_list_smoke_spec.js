'use strict';

var OrderListPage = require('../pages/order_list_page.js');

describe('order list homepage', function () {
    var page;

    beforeEach(function () {
        page = new OrderListPage('https://basta.adeo.no');
    });

/*
    it('should have title BASTA', function () {
        expect(page.title).toEqual('BASTA');
    });

    it('should have some orders', function () {

        expect(page.orderlist.count()).toBeGreaterThan(0);
    });
*/

    it('should have clickable order', function () {
       page.clickFirstOrder();
       console.log("hello");


    });

   /* it('should have clickable order', function () {
            var rows = element.all(by.repeater('order in orders'));
            rows.first().then(function (row){
            var rowElements = row.all(by.tagName('td'));
            rowElements.then(function (columns){
                expect(columns[0].getText()).toBe('5421');
            });
        });

    });
*/


    function printPromise(promise){
        promise.then(function(value){
            console.log(value);
        })
    }
});