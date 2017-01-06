'use strict';

var OrderFormPage = require('../pages/linux_orderform_page')
var loginPage = require('../pages/login_partials')

describe('Basta linux order', function () {
    it('create', function () {
        loginPage.login("user", "user")
        var form = new OrderFormPage()
        form.setDescription("Tester med protractor")
        form.submit().then(function (orderDetails) {
            expect(browser.getCurrentUrl()).toContain('order_details')
            expect(orderDetails.pageHeader()).toContain('Create | Vm | Plain Linux')
            loginPage.logout()
        })
    })
})
