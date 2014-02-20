'use strict';

// Declare app level module which depends on filters, and services
angular.module('skyBestApp', [
    'ngResource',
    'ngRoute',
    'ui.bootstrap',
    'ui.select2',
    'skyBestApp.main_controller',
    'skyBestApp.error_controller',
    'skyBestApp.order_form_controller',
    'skyBestApp.order_list_controller',
    'skyBestApp.node_list_controller'
    ])
  .config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.when('/order', {
            templateUrl: 'partials/order_form.html',
            controller: 'orderFormController'
        });
        $routeProvider.when('/order_list', {
          templateUrl: 'partials/order_list.html',
          controller: 'orderListController'
        });
        $routeProvider.when('/node_list', {
          templateUrl: 'partials/node_list.html',
          controller: 'nodeListController'
        });
        $routeProvider.otherwise({
            redirectTo: '/order_list'
        });
    }])
    .directive('aceEditor', function(){

    var Editor = ace.require("ace/editor").Editor;
    var Renderer = ace.require("ace/virtual_renderer").VirtualRenderer;
    return {
        restrict: "E",
        require: "ngModel",
        replace: true,
        template: "<div class=\"ace-container\"></div>",
        link: function($scope, $el, attrs, model) {
            var editor, session, updateViewValue;
            editor = new Editor(new Renderer($el[0], "ace/theme/monokai"));
            editor.setHighlightActiveLine(true);
            editor.setFontSize(14);
            editor.resize();

            session = editor.getSession();
            session.setMode("ace/mode/xml");
            session.setUseWrapMode(true);

            model.$render = function() {
                return session.setValue(model.$modelValue);
            };
            updateViewValue = function() {
                if(!$scope.$$phase){
                    return $scope.$apply(function() {
                        return model.$setViewValue(session.getValue());
                    });
                }
            };
            session.on("change", updateViewValue);
            return $scope.$on("$destroy", function() {
                return editor.removeListener("change", updateViewValue);
            });
        }
    };
    });
