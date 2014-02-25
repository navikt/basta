'use strict';

describe('order_form_controller', function() {
    //load the controllers module
    beforeEach(module('skyBestApp'));

    var $scope,
        $httpBackend,
        orderFormController;

    var contentTypeXML = {'content-type' : 'application/xml'};
    
    beforeEach(inject(function(_$httpBackend_, $rootScope, $location, $controller) {
        $httpBackend = _$httpBackend_;
        $scope = $rootScope.$new();

        orderFormController = $controller('orderFormController', {
                '$scope': $scope
        });


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

        $httpBackend.whenGET(/rest\/domains\?envClass=.*&zone=fss/).respond(200,'testl.local',{'content-type' : 'application/text'} );
        $httpBackend.whenGET(/rest\/domains\?envClass=.*&zone=sbs/).respond(200,'oera-t.local',{'content-type' : 'application/text'} );
        $httpBackend.whenGET('api/helper/fasit/environments').respond(200, environments, contentTypeXML);
        $httpBackend.whenGET('api/helper/fasit/applications').respond(200, applications, contentTypeXML);
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


        $httpBackend.flush();
        expect($scope.currentUser.username).toBe("the username");
    });

    it ('should accept foreign characters is user name', function(){
        $httpBackend.expectGET('/rest/users/current').respond(
            {username:'The Ææ, The Øø, TheÅå' });

        $httpBackend.flush();
        expect($scope.currentUser.username).toBe("The Ææ, The Øø, TheÅå");
    });

    it('should only be able to click on availiable environmentClasses ', function(){
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses:['u', 't']});

        $httpBackend.flush();
        expect($scope.hasEnvironmentClassAccess('u')).toBe(true);
        expect($scope.hasEnvironmentClassAccess('t')).toBe(true);
        expect($scope.hasEnvironmentClassAccess('p')).toBe(false);
        expect($scope.hasEnvironmentClassAccess('q')).toBe(false);
    });

    it('should only be possible to choose fss when in u environment class', function(){
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses:['u', 't']});

        $httpBackend.flush();
        $scope.settings.environmentClass = 'u';
        $scope.$apply();
        expect($scope.hasZone('fss')).toBe(true);
        expect($scope.hasZone('sbs')).toBe(false);
    });

    it('should be possible to choose both zones when selected environment class is other than U', function(){
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses:['u', 't']});

        $httpBackend.flush();
        $scope.settings.environmentClass = 't';
        $scope.$apply();
        expect($scope.hasZone('fss')).toBe(true);
        expect($scope.hasZone('sbs')).toBe(true);
    });

    function rigBPMNodes(){
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses:['u', 't']});
        $httpBackend.flush();
        $scope.nodeType = 'BPM_NODES';
        $scope.$apply();      //Trigger watch
        $scope.settings.environmentClass = 't';
        $scope.$apply();
        $scope.settings.environmentName = 't0';
        $scope.$apply();
        $httpBackend.whenGET('api/helper/fasit/resources/bestmatch?alias=bpmDmgr&domain=testl.local&envClass=t&envName=t0&type=DeploymentManager').respond(404 );
        $httpBackend.flush();
    };

    it('should set deployment manager not found on formerror when BPM NODE', function(){
        rigBPMNodes();
        expect($scope.settings.nodeType).toBe('BPM_NODES');
        expect($scope.settings.zone).toBe('fss');
        expect($scope.formErrors.general.bpmDeploymentManager).toBe('Deployment manager ikke funnet i gitt miljø');
    });

    it('should remove form errors when changing nodeType', function(){
        rigBPMNodes();
        $scope.nodeType = 'PLAIN_LINUX';
        $scope.$apply();
        expect($scope.settings.nodeType).toBe('PLAIN_LINUX');
        expect($scope.formErrors.general).toEqual({});
    });
});