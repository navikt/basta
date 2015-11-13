'use strict';

module.exports = ['$http', '$location', '$q', 'errorService', function( $http, $location, $q, errorService){

    function flatMap(data, result,key) {
        if(_.isObject(data)) {
            _.each(data, function(val, key) {
                flatMap(val, result, key)})
        } else {
            result[key] = data;
        }
        return result;
    }

    this.submitOrderWithUrl = function(url, data){
    	console.log("posting order to ", url)
        $http.post(url, flatMap(data, {}))
            .success(onOrderSuccess)
            .error(errorService.handleHttpError('Ordreinnsending'));
    };

    function onOrderSuccess(order) {
        $location.path('/order_details/' + order.id)
    }

    return {
        submitOrderWithUrl: this.submitOrderWithUrl
    };

}];

