var basta = require('angular').module('basta');


basta.controller('orderformJbossController', require('./orderform-jboss-controller'));

/*require('./directives');*/


basta.directive('orderformEnvironmentClasses', require('./directives/orderform-environmentclasses'));
basta.directive('orderformEnvironments', require('./directives/orderform-environments'));
basta.directive('orderformApplications', require('./directives/orderform-applications'));
basta.directive('orderformSimples', require('./directives/orderform-simples'));
basta.directive('orderformServersizes', require('./directives/orderform-serversize'));
basta.directive('orderformSubmitButtons', require('./directives/orderform-submit-buttons'));
basta.directive('orderformValidation', require('./directives/orderform-validation'));
basta.directive('orderformZones', require('./directives/orderform-zones'));
