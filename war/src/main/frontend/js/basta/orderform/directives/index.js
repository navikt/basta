var basta = require('angular').module('basta');

basta.directive('orderformEnvironmentClasses', require('./orderform-environmentclasses'));
basta.directive('orderformEnvironments', require('./orderform-environments'));
basta.directive('orderformApplications', require('./orderform-applications'));
basta.directive('orderformSimples', require('./orderform-simples'));
basta.directive('orderformServersizes', require('./orderform-serversize'));
basta.directive('orderformMemorysize', require('./orderform-memorysize'));
basta.directive('orderformExtradisk', require('./orderform-extradisk'));
basta.directive('orderformCpucount', require('./orderform-cpucount'));
basta.directive('orderformSubmitButtons', require('./orderform-submit-buttons'));
basta.directive('orderformValidation', require('./orderform-validation'));
basta.directive('orderformZones', require('./orderform-zones'));
basta.directive('orderform', require('./orderform'));
