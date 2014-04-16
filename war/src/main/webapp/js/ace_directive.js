'use strict';

angular.module('skyBestApp.ace_editor', [])
    .directive('aceEditor', function(){

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
            var editor, session, updateViewValue;
            $scope.theme = $scope.theme || 'monokai';
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
                console.log("Called in render, put on session " + model.$modelValue );
                return session.setValue(model.$modelValue);
            };

            updateViewValue = function() {
                if(!$scope.$$phase){
                    console.log("Called, but yes");
                    return $scope.$apply(function() {
                        console.log("Called, in the apply");
                        return model.$setViewValue(session.getValue());
                    });
                }else{
                    console.log("Called, but no");
                }
            };
            session.on("change", updateViewValue);
            return $scope.$on("$destroy", function() {
                return editor.removeListener("change", updateViewValue);
            });
        }
    };
});