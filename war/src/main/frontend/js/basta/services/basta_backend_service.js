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


    this.serverSizes = function(){
        return  $http({method: 'GET', url: 'rest/vm/choices'})
            .error(errorService.handleHttpError('Valginformasjon'))
            .then(function onSuccess(response) {
                var serverSizes = [
                    { key : 's', name: 'Standard'},
                    { key : 'm', name: 'Medium'},
                    { key : 'l', name: 'Stor'}
                ];
                _(serverSizes).each(function (serverSize) {_.extend(serverSize, response.data.serverSizes[serverSize.key]);});
                return serverSizes;
            });
    };


    this.submitOrder = function(data){
        $http.post('rest/orders',flatMap(data, {}))
            .success(onOrderSuccess)
            .error(errorService.handleHttpError('Ordreinnsending'));
    };

    this.submitEditedOrder = function(orderid, data){
        $http.put('rest/orders/' + orderid, data, {headers: {'Content-type': 'text/plain', 'Accept': 'application/json'}})
            .success(onOrderSuccess)
            .error(errorService.handleHttpError('Ordreinnsending'));
    }

    this.editOrder = function(data){
        return $http.post('rest/orders?prepare=true', flatMap(data,{}))
            .error(errorService.handleHttpError('Ordreinnsending'));
    };

    function onOrderSuccess(order) {
        $location.path('/order_details/' + order.id)
    }

    return {
        serverSizes : this.serverSizes(),
        submitOrder: this.submitOrder,
        submitEditedOrder: this.submitEditedOrder,
        editOrder: this.editOrder
    };


}];
