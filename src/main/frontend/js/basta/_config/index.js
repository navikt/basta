var basta = require('angular').module('basta');

basta.config(['$routeProvider', require('./routes')]);
basta.config(['hotkeysProvider', function(hotkeysProvider){hotkeysProvider.cheatSheetHotkey='q'}]);