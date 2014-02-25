describe('resource_directive', function() {
  var $scope, $compile, $httpBackend;
  beforeEach(function() {
    module('skyBestApp', function($provide) {
      $provide.service('errorService', function() {
        this.handleHttpError = function(name) {
          return function() {
            throw name + ': ' + JSON.stringify(arguments);
          }
        };
      });
    });
  });
  beforeEach(module('skyBestApp.fasit_resource'));
  beforeEach(inject(function (_$rootScope_, _$compile_, _$httpBackend_, $templateCache) {
    $scope = _$rootScope_;    
    $compile = _$compile_;  
    $httpBackend = _$httpBackend_;
    $httpBackend.whenGET('partials/resource_directive.html').respond(204);
//  $httpBackend.whenGET(/rest\/nodes.*/).respond(200, [{id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}], {'content-type' : 'application/text'} );
  }));

  var contentTypeXML = {'content-type' : 'application/xml'};
  var datasources ='<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\
    <collection>\
        <resource>\
            <ref>https://fasit.adeo.no/conf/resources/325</ref>\
            <id>325</id>\
            <type>DataSource</type>\
            <alias>autodeployTestAppUnmanagedDs</alias>\
            <environmentClass>u</environmentClass> \
            <property name="password" type="SECRET">\
                <ref>https://fasit.adeo.no/conf/secrets/secret-2871</ref>\
            </property>\
            <property name="url" type="STRING">\
                <value>thin:oracle:jdbc://dburl.nav.no</value>\
            </property>\
            <property name="username" type="STRING">\
                <value>envconf</value>\
            </property>\
        </resource>\
    </collection>';

  var compileComponent = function (markup, scope) {
    var el = $compile(markup)(scope);      
    scope.$digest();      
    return el;    
  };
  
  it('should call fasit when environment class, domain and environment name are ready', function() {
    var fasitResource = '<fasit-resource ng-model="model" environment-class="environmentClass" environment-name="environmentName" zone="zone"></fasit-resource>';
    var element = compileComponent(fasitResource, $scope.$new());
    var scope = element.scope();
    scope.environmentClass = 'u';
    $httpBackend.flush(0);
    scope.zone = 'fss';
    $httpBackend.flush(0);
    scope.environmentName = 'u1';
    $httpBackend.whenGET('rest/domains?envClass=u&zone=fss').respond(200, 'devillo.no', {'content-type' : 'application/text'} );
    $httpBackend.whenGET('api/helper/fasit/resources?bestmatch=false&domain=devillo.no&envClass=u&envName=u1')
      .respond(200, datasources, contentTypeXML);
    $httpBackend.flush();
    // TODO check scope.choices
  });
});