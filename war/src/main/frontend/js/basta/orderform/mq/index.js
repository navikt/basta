var basta = require('angular').module('basta');

basta.controller('MqQueueFormController', require('./mq-queue-controller'));
basta.controller('MqTopicFormController', require('./mq-topic-controller'));
basta.controller('MqChannelFormController', require('./mq-channel-controller'));

basta.directive('orderformQueueManagers', require('./orderform-queue-managers'));
basta.directive('orderformQueueCluster', require('./orderform-queue-cluster'));

basta.service('queueManagerService', require('./queueManagerService.js'));
