var moment = require('moment');
var _ = require('underscore');
var basta = require('angular').module('basta');

basta.filter('timeago', function () {
    return function (date) {
        return moment(date).fromNow();
    }
});
basta.filter('humanize', function () {
    return function (string) {
        return _(string).chain().humanize().titleize().value();
    }
});

