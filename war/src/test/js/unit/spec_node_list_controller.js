'use strict';

describe('node_list_controller', function () {
    //load the controllers module
    beforeEach(module('skyBestApp'));

    var $scope, $httpBackend;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $location, $controller) {
        $httpBackend = _$httpBackend_;
        $scope = $rootScope.$new();
        $controller('nodeListController', {
            '$scope': $scope
        });
        $httpBackend.whenGET(/rest\/vm\/nodes.*/).respond(200, [
            {id: 1},
            {id: 2},
            {id: 3},
            {id: 4},
            {id: 5}
        ], {'content-type': 'application/text'});
        $httpBackend.whenGET('/rest/users/current').respond(
            {username: 'the username',
                authenticated: true,
                environmentClasses: []
            });
        $httpBackend.flush();
    }));

    it("should retrieve nodes on startup", function () {
        expect(_.isArray($scope.selectedNodes)).toBe(true);
        expect(_.isEmpty($scope.selectedNodes)).toBe(true);
        expect($scope.nodes.length).toBe(5);
    });

    it("should enable select node", function () {
        expect($scope.selectedNodes.length).toBe(0);
        $scope.setSelectedNode({id: 3});
        expect($scope.selectedNodes.length).toBe(1);
    });

});
