var basta = require('angular').module('basta');

basta.controller('orderDetailsController', require('./order_details_controller'));
basta.directive('orderdetailsHeader', require('./orderdetails-header'))
basta.directive('orderdetailsPanel', require('./orderdetails-panel'))

