'use strict';

describe('decommision_form_controller', function () {
    //load the controllers module
    beforeEach(module('skyBestApp'));

    var $scope, rootScope;

    function setUp($httpBackend, $rootScope, $controller, $location, superUser) {
        $httpBackend.expectGET('/rest/users/current').respond({
            superUser: superUser
        });

        $scope = $rootScope.$new();

        $controller('decommisionFormController', {'$scope': $scope, $location: $location, $rootScope: rootScope});
        $httpBackend.flush();
    }

    it('should not be possible to see the decommision page normal user', inject(function ($httpBackend, $location, $controller, $rootScope) {
        setUp($httpBackend, $rootScope, $controller, $location, false);
        expect($scope.superUser).toBe(false);
    }));


    it('should be possible to see the decommision page as superuser', inject(function ($httpBackend, $location, $controller, $rootScope) {
        setUp($httpBackend, $rootScope, $controller, $location, true);
        expect($scope.superUser).toBe(true);
    }));
});