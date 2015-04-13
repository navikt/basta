'use strict';



module.exports = ['$timeout', function($timeout){
    var ace=require('ace');
    require('brace/mode/xml');
    require('brace/theme/monokai');
    require('brace/theme/chrome');
    var Editor = ace.require("ace/editor").Editor;
    var Renderer = ace.require("ace/virtual_renderer").VirtualRenderer;
    return {
        restrict: "E",
        require: "ngModel",
        replace: true,
        template: "<div class=\"ace-container\"></div>",
        scope: {
            theme: '@theme',
            fontsize: '@fontsize',
            readonly: '@readonly'
        },
        link: function($scope, $el, attrs, model) {
            var editor, session;
            $scope.theme = 'monokai';
            $scope.fontsize = $scope.fontsize || 14;
            $scope.readonly = $scope.readonly || false;

            editor = new Editor(new Renderer($el[0], 'ace/theme/'+$scope.theme));
            editor.setHighlightActiveLine(true);
            editor.setFontSize($scope.fontsize);
            editor.setReadOnly($scope.readonly);

            editor.resize();

            session = editor.getSession();
            session.setMode("ace/mode/xml");
            session.setUseWrapMode(true);

            model.$render = function() {
                return session.setValue(model.$modelValue);
            };

            function updateViewValue() {
                 return model.$setViewValue(session.getValue());

            };

            session.on("change", function(){
                $timeout(function(){
                    if(!_.isEmpty(session.getValue())){
                        $scope.$apply(updateViewValue);
                    }
                },0);
            });

            return $scope.$on("$destroy", function() {
                return editor.removeListener("change");
            });
        }
    };
}];