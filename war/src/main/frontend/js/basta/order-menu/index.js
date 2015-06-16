var basta = require('angular').module('basta');
basta.controller('orderMenuController', require('./order-menu-controller'));
//@deprecated
basta.directive('menuItem', require('./menu-item'));
