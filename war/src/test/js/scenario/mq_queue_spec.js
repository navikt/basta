'use strict';

var MqOrderPage = require('../pages/mq_queue_order_page');
var OperationPage = require('../pages/mq_queue_operation_page');
var loginPage = require('../pages/login_partials');


describe('Basta mq queue order', function () {
    beforeEach(function () {
        loginPage.login("user", "user");
    })

    afterEach(function () {
        loginPage.logout()
    })

    it('has correct title', function () {
        var orderPage = new MqOrderPage();
        expect(orderPage.formTitle()).toEqual('MQ')
    })

    it('should create mq queue ok', function () {
        var orderPage = new MqOrderPage();
        orderPage.setEnvironment('cd-u1');
        orderPage.setApplication('fasit');
        orderPage.setQueueName("E2EQueue");
        orderPage.setQueueMananger("CLIENT0");
        orderPage.submit().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Create | Mq | Queue |');
        });
    });

    it('should create mq queue with valdiation error', function () {
        var orderPage = new MqOrderPage();
        orderPage.setEnvironment('cd-u1');
        orderPage.setApplication('fasit');
        orderPage.setQueueName("EXISTS");
        orderPage.setQueueMananger("CLIENT0");
        orderPage.submit().then(function (orderDetails) {
            expect(orderPage.hasValidationError());
        });
    });
});

describe('Basta mq queue operations', function () {
    beforeEach(function () {
        loginPage.login("user", "user");
    })

    afterEach(function () {
        loginPage.logout()
    })

    it('stop mq queue', function () {
        var operations = new OperationPage();
        operations.setQueueMananger("CLIENT0");
        operations.setQueueName("U1_MOCK_QUEUE2");
        operations.stop().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Stop | Mq');
        });
    });
    it('start mq queue', function () {
        var operations = new OperationPage();
        operations.setQueueMananger("CLIENT0");
        operations.setQueueName("U1_MOCK_QUEUE2");
        operations.start().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Start | Mq');
        });
    });
    it('delete mq queue', function () {
        var operations = new OperationPage();
        operations.setQueueMananger("CLIENT0");
        operations.setQueueName("U1_MOCK_QUEUE2");
        operations.remove().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details');
            expect(orderDetails.pageHeader()).toContain('Delete | Mq');
        });
    });

});
