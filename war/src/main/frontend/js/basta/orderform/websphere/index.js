var basta = require('angular').module('basta');

basta.controller('wasNodeController', require('./was-node-controller'));
basta.controller('wasDmgrController', require('./was-dmgr-controller'));
//@deprecated
basta.controller('orderFormController', require('./order_form_controller'));