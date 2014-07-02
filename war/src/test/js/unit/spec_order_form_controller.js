'use strict';

describe('order_form_controller', function () {
    //load the controllers module
    beforeEach(module('skyBestApp'));

    var $scope,
        $httpBackend,
        $rootScope,
        location,
        orderFormController,
        bestMatchResponse;

    var contentTypeXML = {'Content-type': 'application/xml', 'Accept': 'application/json, text/plain, */*'};
    var contentTypePlain = {"Content-type": "text/plain", "Accept": "application/json"};
    beforeEach(inject(function (_$httpBackend_, _$rootScope_, $location, $controller) {
        $httpBackend = _$httpBackend_;
        location = $location;
        $scope = _$rootScope_.$new();
        $rootScope = _$rootScope_;

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

        bestMatchResponse = [200, '', {}];
        $httpBackend.whenGET(/api\/helper\/fasit\/resources\/\?bestmatch=true.*/).respond(function (method, url, data, headers) {
            return bestMatchResponse;
        });
        $httpBackend.whenGET(/rest\/domains\?envClass=.*&zone=fss/).respond(200, 'testl.local', {'content-type': 'application/text'});
        $httpBackend.whenGET(/rest\/domains\?envClass=.*&zone=sbs/).respond(200, 'oera-t.local', {'content-type': 'application/text'});

        $httpBackend.whenGET(/rest\/domains\/multisite\?envClass=.*&envName=.*/).respond(200, false);

        $httpBackend.whenGET('api/helper/fasit/environments').respond(200, environments, contentTypeXML);
        $httpBackend.whenGET('api/helper/fasit/applications').respond(200, applications, contentTypeXML);
        $httpBackend.whenGET('rest/choices').respond(
            {serverSizes: {xl: {
                externDiskMB: 40960,
                ramMB: 16384,
                cpuCount: 2}
            }
            });
    }));

    it('should retrieve user', function () {
        $httpBackend.expectGET('/rest/users/current').respond(
            {username: 'the username',
                authenticated: true,
                environmentClasses: []
            });


        $httpBackend.flush();
        expect($scope.currentUser.username).toBe("the username");
    });

    it('should accept foreign characters is user name', function () {
        $httpBackend.expectGET('/rest/users/current').respond(
            {username: 'The Ææ, The Øø, TheÅå' });

        $httpBackend.flush();
        expect($scope.currentUser.username).toBe("The Ææ, The Øø, TheÅå");
    });

    it('should only be able to click on availiable environmentClasses ', function () {
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses: ['u', 't']});

        $httpBackend.flush();
        expect($scope.hasEnvironmentClassAccess('u')).toBe(true);
        expect($scope.hasEnvironmentClassAccess('t')).toBe(true);
        expect($scope.hasEnvironmentClassAccess('p')).toBe(false);
        expect($scope.hasEnvironmentClassAccess('q')).toBe(false);
    });

    it('should only be possible to choose fss when in u environment class', function () {
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses: ['u', 't']});

        $httpBackend.flush();
        $scope.settings.environmentClass = 'u';
        $scope.$apply();
        expect($scope.hasZone('fss')).toBe(true);
        expect($scope.hasZone('sbs')).toBe(false);
    });

    it('should be possible to choose both zones when selected environment class is other than U', function () {
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses: ['u', 't']});

        $httpBackend.flush();
        $scope.settings.environmentClass = 't';
        $scope.$apply();
        expect($scope.hasZone('fss')).toBe(true);
        expect($scope.hasZone('sbs')).toBe(true);
    });


    function applyOnScope(path, value) {
        withObjectInPath($scope, path, function (object, property) {
            object[property] = value;
            $scope.$apply();
        });
    }

    function expectDefaultEnvironmentClassesForUser() {
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses: ['u', 't']});
        $httpBackend.flush();
    }

    it('should set deployment manager not found on formerror when BPM NODE', function () {
        bestMatchResponse = [404, '', {}];
        expectDefaultEnvironmentClassesForUser();
        applyOnScope(['nodeType'], 'BPM_NODES');
        applyOnScope(['settings', 'environmentClass'], 't');
        applyOnScope(['settings', 'environmentName'], 't0');

        $httpBackend.flush();


        expect($scope.nodeType).toBe('BPM_NODES');
        expect($scope.settings.zone).toBe('fss');
        expect($scope.formErrors.deploymentManager).toBe('BPM Deployment Manager ikke funnet i gitt miljø og sone');
    });

    it('should remove form errors when changing nodeType', function () {
        bestMatchResponse = [200, '', {}];
        expectDefaultEnvironmentClassesForUser();
        applyOnScope(['nodeType'], 'BPM_NODES');
        applyOnScope(['settings', 'environmentClass'], 't');
        applyOnScope(['settings', 'environmentName'], 't0');
        $httpBackend.flush();

        applyOnScope(['nodeType'], 'PLAIN_LINUX');

        expect($scope.nodeType).toBe('PLAIN_LINUX');
        expect($scope.formErrors.general).toEqual({});
    });

    it('should not be ready', function () {
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses: ['u', 't']});
        $httpBackend.flush();
        expect($scope.isValidForm()).toBe(0);
    });

    function setUpValidForm() {
        $httpBackend.expectGET('/rest/users/current').respond({environmentClasses: ['u', 't']});
        $httpBackend.flush();

        $rootScope.alive = true;
        $rootScope.$apply();

        applyOnScope(['settings', 'environmentName'], 'u1');
        applyOnScope(['settings', 'applicationName'], 'basta');
        applyOnScope(['settings', 'middleWareType'], 'jb');
        applyOnScope(['currentUser', 'authenticated'], true);
    }

    it('should be ready', function () {
        setUpValidForm();
        expect($scope.isValidForm()).toBe(true);
    });

    it('should post order when valid form', function () {
        setUpValidForm();
        var data = {
            "environmentClass": "u",
            "zone": "fss",
            "environmentName": "u1",
            "applicationName": "basta",
            "serverCount": 1,
            "serverSize": "s",
            "disks": 0,
            "middleWareType": "jb",
            "nodeType": "APPLICATION_SERVER"
        };

        $scope.submitOrder();
        $httpBackend.expectPOST('rest/orders', data).respond({id: 1});
        $httpBackend.flush();

        expect(location.url()).toBe('/order_list?id=1');
    });


    it('should put prepared.xml when prepared exists on scope', function () {
        setUpValidForm();
        var data = '<xml/>';

        $scope.prepared = {xml: data, orderId: '1'};
        $scope.$apply();

        $scope.submitOrder();
        $httpBackend.expectPUT('rest/orders/1', data, contentTypePlain).respond({id: 1});
        $httpBackend.flush();

        expect(location.url()).toBe('/order_list?id=1');

    });

    it('should not display plain linux as a alternative when logged in without super user', function () {
        setUpValidForm();
        expect(_.keys($scope.choices.defaults)).not.toContain("PLAIN_LINUX");
    });

    it('should display plain linux as a alternative when logged in as super user', function () {
        $httpBackend.expectGET('/rest/users/current').respond({superUser: true});
        $httpBackend.flush();
        expect(_.keys($scope.choices.defaults)).toContain("PLAIN_LINUX");
    });
});