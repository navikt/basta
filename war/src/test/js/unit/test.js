'use strict';

describe('order_form_controller', function() {
    //load the controllers module
    beforeEach(module('skyBestApp'));

    var $scope, 
        $httpBackend, 
        orderFormController;

    beforeEach(inject(function(_$httpBackend_, $rootScope, $location, $controller) {
        $httpBackend = _$httpBackend_;
        $scope = $rootScope.$new();

        orderFormController = function() {
            return $controller('orderFormController', {'$scope': $rootScope});
        };

        var environments = 
            '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\
            <collection>\
                <environment>\
                    <envClass>p</envClass>\
                    <name>p</name>\
                </environment>\
            </collection>';

        var applications = 
            '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\
            <collection>\
                <application>\
                    <appConfigArtifactId>a</appConfigArtifactId>\
                    <appConfigGroupId>b</appConfigGroupId> \
                    <name>c</name>\
                </application>\
            </collection>';

        var contentType = {'content-type' : 'application/xml'};

        $httpBackend.whenGET('api/helper/fasit/environments').respond(200, environments, contentType);
        $httpBackend.whenGET('api/helper/fasit/applications').respond(200, applications, contentType);
        $httpBackend.whenGET('rest/choices').respond(
            {serverSizes:
                {xl: {
                    externDiskMB:40960,
                    ramMB:16384,
                    cpuCount:2}
                }
            });
    }));

    it ('should retrieve user', function(){
            $httpBackend.expectGET('/rest/users/current').respond(
                {username:'the username',
                authenticated:true,
                environmentClasses:[]
            });

        var controller = orderFormController();
        $httpBackend.flush();
        expect($scope.currentUser.username).toBe("the username");
    });

it ('should accept foreign characters is user name', function(){
            $httpBackend.expectGET('/rest/users/current').respond(
             {username:'The Ææ, The Øø, TheÅå' });

        var controller = orderFormController();
        $httpBackend.flush();
        expect($scope.currentUser.username).toBe("The Ææ, The Øø, TheÅå");
    });

  it('should only be able to click on availiable environmentClasses ', function(){
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses:['u', 't']});

        orderFormController();
        $httpBackend.flush();

        expect($scope.hasEnvironmentClassAccess('u')).toBe(true);
        expect($scope.hasEnvironmentClassAccess('t')).toBe(true);
        expect($scope.hasEnvironmentClassAccess('p')).toBe(false);
        expect($scope.hasEnvironmentClassAccess('q')).toBe(false);

    });
  
});