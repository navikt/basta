module.exports = function($routeProvider){
    $routeProvider.when('/serviceuser_order',               { templateUrl: 'js/basta/orderform/serviceuser/serviceuser_order_form.html'});
    $routeProvider.when('/serviceuser_certificate_order',   { templateUrl: 'js/basta/orderform/serviceuser/serviceuser_certificate_order_form.html'});
    $routeProvider.when('/vm_order',                        { templateUrl: 'js/basta/orderform/websphere/order_form_vm.html'});
    $routeProvider.when('/jboss_order',                     { templateUrl: 'js/basta/orderform/jboss/jboss-orderform.html'});
    $routeProvider.when('/menu',                            { templateUrl: 'js/basta/order-menu/order-menu.html'});
    $routeProvider.when('/order-operation',                 { templateUrl: 'js/basta/order-operation/order-operation-form.html'});
    $routeProvider.when('/notifications',                   { templateUrl: 'js/basta/notifications/notifications.html'});
    $routeProvider.when('/order_list',                      { templateUrl: 'js/basta/orderlist/order_list.html'});
    $routeProvider.when('/order_details/:id',               { templateUrl: 'js/basta/orderdetails/order_details.html'});
    $routeProvider.when('/changelog',                       { templateUrl: 'js/basta/changelog/changelog.html'});
    $routeProvider.otherwise(                               { redirectTo: '/order_list'});
};
