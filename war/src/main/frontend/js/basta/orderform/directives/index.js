var basta = require('angular').module('basta');

basta.directive('orderformEnvironmentClasses', require('./orderform-environmentclasses'));
basta.directive('orderformEnvironments', require('./orderform-environments'));
basta.directive('orderformApplications', require('./orderform-applications'));
basta.directive('orderformQueueManagers', require('./orderform-queue-managers'));
basta.directive('orderformQueueName', require('./orderform-queue-name'));
basta.directive('orderformDropdown', require('./orderform-dropdown'));
basta.directive('orderformTextinput', require('./orderform-textinput'));
basta.directive('orderformButtons', require('./orderform-buttons'));
basta.directive('orderformValidation', require('./orderform-validation'));
basta.directive('orderformZones', require('./orderform-zones'));
basta.directive('orderform', require('./orderform'));
basta.directive('orderformAlert', require('./orderform-alert'));
basta.directive('orderformAlertError', require('./orderform-alert-error'));
basta.directive('orderformClassification', require('./orderform-classification'));
