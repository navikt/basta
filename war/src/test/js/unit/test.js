'use strict';

describe("hello", function(){
	it('should be true', function(){
		expect(true).toBe(true);

	})
});


describe('order_form_controller', function() {
    beforeEach(module('skyBestApp'));
    var $scope, $location, $rootScope, $httpBackend, createController;

    beforeEach(inject(function($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.expectGET('/rest/users/current').respond({"username":"herr Eldby","authenticated":true,"environmentClasses":[]});
        $httpBackend.expectGET('api/helper/fasit/environments').respond(
            {collection:
                [{environment : {
                    envClass: "q", 
                    name : "q6" }
                }]
            });
      $httpBackend.expectGET('api/helper/fasit/applications').respond(
          {collection:
              [{application : {
                  appConfigArtifactId: "a", 
                  appConfigGroupId:"b", 
                  name : "c" }
              }]
          });

        $httpBackend.expectGET('rest/choices').respond(
            {serverSizes:
                {xl: {
                    externDiskMB:40960,
                    ramMB:16384,
                    cpuCount:2}
                }
            });

        $rootScope = $injector.get('$rootScope');
        $scope = $rootScope.$new();
        $location = $injector.get('$location');
        var $controller = $injector.get('$controller');
        
        createController = function() {
            return $controller('orderFormController', {'$scope': $rootScope});
        };
    }));

    it ('should retrieve user on startup', function(){

        var controller = createController();
        $httpBackend.flush();
        
        expect($scope.currentUser.username).toBe("herr Eldby");

    })
});