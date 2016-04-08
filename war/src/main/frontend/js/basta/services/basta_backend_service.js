'use strict';

module.exports = ['$http', '$location', '$q', 'errorService', function ($http, $location, $q, errorService) {

    function flatMap(data, result, key) {
        if (_.isObject(data)) {
            _.each(data, function (val, key) {
                flatMap(val, result, key)
            })
        } else {
            result[key] = data;
        }
        return result;
    }

    this.submitOrderWithUrl = function (url, data) {
        $http.post(url, flatMap(data, {}))
            .success(onOrderSuccess)
            .error(errorService.handleHttpError('Ordreinnsending'));
    };

    
    this.putOrder = function (url, data) {
        console.log("putting order to ", url)
        $http.put(url, data)
            .success(onOrderSuccess)
            .error(errorService.handleHttpError('Ordreinnsending'));
    };
    
    this.postOrder = function (url, data) {
        console.log("posting order to ", url)
        $http.post(url, data)
            .success(onOrderSuccess)
            .error(errorService.handleHttpError('Ordreinnsending'));
    };



    function onOrderSuccess(order) {
        var orderid = order.id;
        if (!orderid) {
            // returnerer ikke hele order objektet
            orderid = order.orderId;
        }
        $location.path('/order_details/' + orderid)
    }

    return {
        submitOrderWithUrl: this.submitOrderWithUrl,
        putOrder: this.putOrder,
        postOrder: this.postOrder,
        redirectToDetails: this.onOrderSuccess
    };

}];

