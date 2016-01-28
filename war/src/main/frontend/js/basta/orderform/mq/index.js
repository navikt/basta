var basta = require('angular').module('basta');

basta.controller('MqQueueFormController', require('./mq-queue-controller'));
basta.controller('MqChannelFormController', require('./mq-channel-controller'));

basta.directive('orderformQueueManagers', require('./orderform-queue-managers'));
basta.directive('orderformMqName', require('./orderform-mq-name'));