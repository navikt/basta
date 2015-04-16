'use strict';
module.exports = ['$http', function ($http) {
        return {
            restrict: 'E',
            link: function (scope, element, attrs) {
                $http.get('/changelog/' + attrs.version + '.md').then(function (data) {
                    var markdown = data.data;
                    var Showdown = require('showdown');
                    var converter =  new Showdown.converter();
                    var htmlText = converter.makeHtml(markdown);
                    element.html(htmlText);
                })
            }
        }
    }];