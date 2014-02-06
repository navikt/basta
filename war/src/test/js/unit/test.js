'use strict';

describe("hello", function(){
	it('should be true', function(){
		expect(true).toBe(true);

	})
});


describe('order_form_controller', function() {
	beforeEach(module('skyBestApp'));

    var $scope, $location, $rootScope, createController;

    beforeEach(inject(function($injector) {
        $location = $injector.get('$location');
        $rootScope = $injector.get('$rootScope');
        $scope = $rootScope.$new();

        var $controller = $injector.get('$controller');

        createController = function() {
            return $controller('orderFormController', {
                '$scope': $scope
            });
        };
    }));

    it('something', function() {
        var controller = createController();
        expect($scope.status).toBe('Loading order...');
      
    });
});