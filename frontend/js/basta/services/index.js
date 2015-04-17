var basta = require('angular').module('basta');

basta.service('BastaService', require('./basta_backend_service'));
basta.service('FasitService', require('./fasit_service'));
basta.service('notificationService', require('./notification_service'));
basta.service('User', require('./user_service'));
basta.service('errorService', require('./error_service'));
basta.factory('accessChecker', require('./access_checker_service'));
