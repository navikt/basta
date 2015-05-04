module.exports = function($routeProvider){
    $routeProvider.when('/serviceuser_order',               { templateUrl: 'basta/orderform/serviceuser/serviceuser_order_form.html'});
    $routeProvider.when('/serviceuser_certificate_order',   { templateUrl: 'basta/orderform/serviceuser/serviceuser_certificate_order_form.html'});
    $routeProvider.when('/serviceuser_credential_order',   { templateUrl: 'basta/orderform/serviceuser/serviceuser_credential_order_form.html'});
    $routeProvider.when('/vm_order',                        { templateUrl: 'basta/orderform/websphere/order_form_vm.html'});
    $routeProvider.when('/jboss_order',                     { templateUrl: 'basta/orderform/jboss/jboss-orderform.html'});
    $routeProvider.when('/menu',                            { templateUrl: 'basta/order-menu/order-menu.html'});
    $routeProvider.when('/order-operation',                 { templateUrl: 'basta/order-operation/order-operation-form.html'});
    $routeProvider.when('/notifications',                   { templateUrl: 'basta/notifications/notifications.html'});
    $routeProvider.when('/order_list',                      { templateUrl: 'basta/orderlist/order_list.html'});
    $routeProvider.when('/order_details/:id',               { templateUrl: 'basta/orderdetails/order_details.html'});
    $routeProvider.when('/changelog',                       { templateUrl: 'basta/changelog/changelog.html'});
    $routeProvider.otherwise(                               { redirectTo: '/order_list'});
};
