angular.module('toggle-switch', ['ng'])
    .directive('toggleSwitch', function() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            model: '=',
            disabled: '@',
            onLabel: '@',
            offLabel: '@',
            knobLabel: '@',
            size: '@',
            colorLeft: '@',
            colorRight: '@'
        },
        template: '<div class="switch" ng-click="toggle()" ng-class="{ \'disabled\': disabled }">' +
            '<div class="switch-animate" ng-class="{\'switch-off\': !model, \'switch-on\': model}">' +
            '<span class="{{colorLeft}} {{size}}" ng-bind="onLabel"></span>' +
            '<span class="knob {{size}}" ng-bind="knobLabel"></span>' +
            '<span class="{{colorRight}} {{size}}" ng-bind="offLabel"></span></div></div>',
        controller: function($scope) {
            $scope.toggle = function toggle() {
                if(!$scope.disabled) {
                    $scope.model = !$scope.model;
                    var out = $scope.model? true : false
                    $scope.$emit("toggled", out);

                }
            };
        },
        compile: function(element, attrs) {
            if (!attrs.onLabel) { attrs.onLabel = '.'; }
            if (!attrs.offLabel) { attrs.offLabel = '.'; }
            if (!attrs.knobLabel) { attrs.knobLabel = '\u00a0'; }
            if (!attrs.disabled) { attrs.disabled = false; }
            if (!attrs.colorLeft){
                attrs.colorLeft ='switch-primary';
            }else{
                attrs.colorLeft ='switch-'+attrs.colorLeft;
            }
            if (!attrs.colorRight){
                attrs.colorRight ='switch-primary';
            }else{
                attrs.colorRight ='switch-'+attrs.colorRight;
            }
        }
    };
});