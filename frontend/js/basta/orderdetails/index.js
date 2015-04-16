var basta = require('angular').module('basta');

basta.controller('orderDetailsController', require('./order_details_controller'));
basta.directive('orderdetailsHeader', require('./directives/orderdetails-header'))
basta.directive('orderdetailsPanel', require('./directives/orderdetails-panel'))

