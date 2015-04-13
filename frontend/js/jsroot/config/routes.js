module.exports = function($routeProvider){
    $routeProvider.when('/serviceuser_order',               { templateUrl: 'partials/serviceuser/serviceuser_order_form.html'});
    $routeProvider.when('/serviceuser_certificate_order',   { templateUrl: 'partials/serviceuser/serviceuser_certificate_order_form.html'});
    $routeProvider.when('/vm_order',                        { templateUrl: 'partials/order_form_vm.html'});
    $routeProvider.when('/jboss_order',                     { templateUrl: 'partials/orderform/orderform-jboss.html'});
    $routeProvider.when('/menu',                            { templateUrl: 'partials/order_menu.html'});
    $routeProvider.when('/decommision',                     { templateUrl: 'partials/decommision_form.html'});
    $routeProvider.when('/notifications',                   { templateUrl: 'partials/notifications.html'});
    $routeProvider.when('/order_list',                      { templateUrl: 'partials/order_list.html'});
    $routeProvider.when('/order_details/:id',               { templateUrl: 'partials/order_details.html'});
    $routeProvider.when('/changelog',                       { templateUrl: 'partials/changelog.html'});
    $routeProvider.otherwise(                               { redirectTo: '/order_list'});
};
