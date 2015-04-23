'use strict';

module.exports = ['$http', '$location', 'errorService', function ($http, $location, errorService) {

    return {
        restrict: 'E',
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: "basta/directives/modalconfirm.html",
        scope: {
            operation: '=',
            target: '='
        },
        controller: function () {
            this.actions = {
                START: {
                    'header': 'Start',
                    'message': 'Do you really want to start ',
                    'url': 'rest/vm/nodes/start'
                },
                STOP: {
                    'header': 'Stop',
                    'message': 'Do you really want to stop ',
                    'url': 'rest/vm/nodes/stop'
                },
                DELETE: {
                    'header': 'Decommission',
                    'message': 'Do you really want to decommission  ',
                    'url': 'rest/vm/nodes/decommission'
                }
            };


        },
        link: function (scope, elem, attrs) {
            scope.$watch('ctrl.operation', function (value) {
                if (value) {
                    scope.ctrl.header = scope.ctrl.actions[value].header;
                    scope.ctrl.message = scope.ctrl.actions[value].message + " " + scope.ctrl.target + "?";
                    scope.ctrl.url = scope.ctrl.actions[value].url;
                }
            });
            scope.ok = function () {
                $("#modal").modal('hide').on('hidden.bs.modal', function () {
                    var target = (_(scope.ctrl.target).isArray) ? scope.ctrl.target :scope.ctrl.target.split(',');
                    $http.post(scope.ctrl.url, target).success(function (result) {
                        $location.path('/order_details/'  + result.orderId);
                    }).error(errorService.handleHttpError(scope.ctrl.header, 'orderSend'));
                });
            };
            scope.cancel = function () {
                scope.ctrl.operation = '';
            };

        }
    };

}];
