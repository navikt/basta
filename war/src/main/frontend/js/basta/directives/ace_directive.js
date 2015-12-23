'use strict';


module.exports = ['$timeout', function ($timeout) {
    var ace = require('brace');
    require('brace/mode/xml');
    require('brace/theme/merbivore_soft');
    return {
        restrict: "E",
        require: "ngModel",
        replace: true,
        template: "<div class=\"ace-container\" id =\"ace\"></div>",
        scope: {
            theme: '@theme',
            fontsize: '@fontsize',
            readonly: '@readonly'
        },
        link: function ($scope, $el, attrs, model) {

            var editor = ace.edit("ace");

            editor.setTheme('ace/theme/merbivore_soft');

            var readonly = $scope.readonly || false;
            var fontSize = parseInt($scope.fontsize) || 14;

            editor.setReadOnly(readonly);
            editor.setFontSize(fontSize);
            editor.setHighlightActiveLine(true);

            var session = editor.getSession();
            session.setUseWrapMode(true);
            session.setMode('ace/mode/xml');


            model.$render = function () {
                return editor.setValue(model.$modelValue);
            };

            function updateViewValue() {
                return model.$setViewValue(editor.getValue());

            };

            editor.on("change", function () {
                $timeout(function () {
                    if (!_.isEmpty(editor.getValue())) {
                        $scope.$apply(updateViewValue);
                        editor.clearSelection();

                    }
                }, 0);
            });

            return $scope.$on("$destroy", function () {
                return editor.removeListener("change");
            });
        }
    };
}];