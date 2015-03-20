'use strict';

angular.module('basta.basta_backend_service', []).service('BastaService', ['$http', '$q', 'errorService', function($http,$q, errorService){


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



    return {
        serverSizes : this.serverSizes()
    };


}]);

