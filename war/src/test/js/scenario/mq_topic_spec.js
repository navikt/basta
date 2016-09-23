'use strict';

var MqOrderPage = require('../pages/mq_topic_order_page');
var OperationPage = require('../pages/mq_topic_operation_page');
var loginPage = require('../pages/login_partials');

describe('basta mq topic order', function () {
    beforeEach(function () {
        loginPage.login("user", "user");
    })

    afterEach(function () {
        loginPage.logout()
    })

    it('creates mq topic', function () {
        var orderPage = new MqOrderPage();
        orderPage.setEnvironment('cd-u1');
        orderPage.setApplication('fasit');
        orderPage.setQueueMananger("CLIENT0");
        orderPage.setTopicString("u1/my/topic");
        orderPage.submit().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Create | Mq | Topic |');
        });
    });

    it('fails creation with invalid form', function () {
        var orderPage = new MqOrderPage();
        orderPage.setEnvironment('cd-u1');
        orderPage.setApplication('fasit');
        orderPage.setQueueMananger("CLIENT0");
        orderPage.setTopicString("mock/me/to/hell");
        orderPage.submit().then(function (orderDetails) {
            expect(orderPage.hasValidationError());
        });
    });
});

describe('basta mq topic operations', function () {
    beforeEach(function () {
        loginPage.login("user", "user");
    })

    afterEach(function () {
        loginPage.logout()
    })

    it('stop mq topic', function () {
        var operations = new OperationPage();
        operations.setQueueMananger("CLIENT0");
        operations.setTopicString("mock/me/to/hell");
        operations.stop().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Stop | Mq');
        });
    });
    it('start mq topic', function () {
        var operations = new OperationPage();
        operations.setQueueMananger("CLIENT0");
        operations.setTopicString("mock/me/to/hell");
        operations.start().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Start | Mq');
        });
    });
    it('delete mq topic', function () {
        var operations = new OperationPage();
        operations.setQueueMananger("CLIENT0");
        operations.setTopicString("mock/me/to/hell");
        operations.remove().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Delete | Mq');
        });
    });

});
