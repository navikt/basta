'use strict';

var angular = require('angular');

module.exports = [ '$routeParams', 'BastaService', function($routeParams, BastaService) {

    this.data = {
	environmentClass : 'u',
	zone : 'fss',
	hostname: $routeParams.hostname,
    }

    this.submitOrder = function() {
	console.log("creating new order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/operations', this.data);
    };

} ];
