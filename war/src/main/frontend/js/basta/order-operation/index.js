require('./directives');
require('./vm');

var basta = require('angular').module('basta');

basta.controller('orderoperationFormController', require('./order-operation-controller'));
