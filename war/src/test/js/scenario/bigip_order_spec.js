'use strict';

// var BigIPOrderPage = require('../pages/bigip_order_page');
// var loginPage = require('../pages/login_partials');
//
// describe('Basta BigIP order', function () {
//
//     beforeEach(function () {
//         loginPage.login("user", "user")
//     })
//
//     afterEach(function () {
//         loginPage.logout()
//     })
//
//     it('should create big ip config with context roots ok', function () {
//         var orderPage = new BigIPOrderPage("/#/bigip_order");
//         orderPage.setEnvironment('cd-u1');
//         orderPage.setApplication('fasit');
//         orderPage.setVirtualServer('vs_name_1');
//         orderPage.setContextRoot("protractor, context, roots");
//         orderPage.setHostname("http://an.optional.dns.for.good.measure");
//         orderPage.submit().then(function (orderDetails) {
//             expect(browser.getCurrentUrl()).toContain('order_details');
//             expect(orderDetails.pageHeader()).toContain('Create | Bigip | Big Ip Config');
//         });
//         console.log("done with first ");
//     });
//
//     it('should fail creating big ip config with context roots when missing context roots', function () {
//         var orderPage = new BigIPOrderPage("/#/bigip_order");
//         orderPage.setEnvironment('cd-u1');
//         orderPage.setApplication('fasit');
//         orderPage.setVirtualServer('vs_name_1');
//         orderPage.setHostname("http://an.optional.dns.for.good.measure");
//         orderPage.submit().then(function () {
//             expect(browser.getCurrentUrl()).toContain('bigip_order');
//         });
//     });
//
//     it('should create big ip config with hostname ok', function () {
//         var orderPage = new BigIPOrderPage("/#/bigip_order?useHostnameMatching=true");
//         orderPage.setEnvironment('cd-u1');
//         orderPage.setApplication('fasit');
//         orderPage.setVirtualServer('vs_name_1');
//         orderPage.setHostname("hostname.domain.tld");
//         orderPage.submit().then(function (orderDetails) {
//             expect(browser.getCurrentUrl()).toContain('order_details');
//             expect(orderDetails.pageHeader()).toContain('Create | Bigip | Big Ip Config');
//         });
//     });
//
//     it('should fail creating big ip config with context roots when missing hostname', function () {
//         var orderPage = new BigIPOrderPage("/#/bigip_order?useHostnameMatching=true");
//         orderPage.setEnvironment('cd-u1');
//         orderPage.setApplication('fasit');
//         orderPage.setVirtualServer('vs_name_1');
//         orderPage.submit().then(function () {
//             expect(browser.getCurrentUrl()).toContain('bigip_order');
//         });
//     });
//
// });