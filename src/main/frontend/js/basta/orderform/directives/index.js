var basta = require('angular').module('basta');

basta.directive('orderformEnvironmentClasses', require('./orderform-environmentclasses'));
basta.directive('orderformEnvironments', require('./orderform-environments'));
basta.directive('orderformApplications', require('./orderform-applications'));
basta.directive('orderformDropdown', require('./orderform-dropdown'));
basta.directive('orderformTextinput', require('./orderform-textinput'));
basta.directive('orderformButtons', require('./orderform-buttons'));
basta.directive('orderformValidation', require('./orderform-validation'));
basta.directive('orderformZones', require('./orderform-zones'));
basta.directive('orderform', require('./orderform'));
basta.directive('orderformAlert', require('./orderform-alert'));
basta.directive('orderformAlertError', require('./orderform-alert-error'));
basta.directive('orderformClassification', require('./orderform-classification'));
basta.directive('orderformCheckbox', require('./orderform-checkbox'));
basta.directive('orderformGeneratedText', require('./orderform-generated-text'));
basta.directive('orderformMultivalue', require('./orderform-multivalue'));