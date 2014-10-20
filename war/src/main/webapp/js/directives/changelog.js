'use strict';

angular.module('changelog', [])
    .directive('changelog', ['$http', function ($http) {
        var converter = new Showdown.converter();
        return {
            restrict: 'E',
            link: function (scope, element, attrs) {
                $http.get('/changelog/' + attrs.version + '.md').then(function (data) {
                    var markdown = data.data;
                    var htmlText = converter.makeHtml(markdown);
                    element.html(htmlText);
                })
            }
        }
    }]);