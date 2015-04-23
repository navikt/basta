var basta = require('angular').module('basta');

basta.directive('aceEditor', require('./ace_directive'));
basta.directive('changelogMarkdown', require('./changelog-markdown'));
basta.directive('iconOperation', require('./icon-operation'));
basta.directive('iconStatus', require('./icon-status'));
basta.directive('infiniteScroll', require('./ngInfiniteScroll'));
basta.directive('fasitResource', require('./resource_directive'));
basta.directive('timer', require('./timer'));
basta.directive('checkbox', require('./angular-bootstrap-checkbox'))
basta.directive('modalconfirm', require('./modalconfirm-directive'));