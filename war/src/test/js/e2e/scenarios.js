describe('bestSkyApp', function() {
    describe('Order View', function() {
        beforeEach(function() {
            browser().navigateTo('../index.html');
        });
        it('should display orders list when accessing index.html', function() {
            expect(browser().location().url()).toBe('/order_list')
        });
    });

    describe('Load applications', function(){


        beforeEach(function() {

           // var http = require("http");
          //  var server = http.createServer(function(request, response) {
          //      response.end('<collection><application><name>Tullball</name></application></collection>');});
         //   server.listen(8000);
         //   console.log("Server is listening");
            browser().navigateTo('../order');
            $httpBackend.whenGET('api/helper/fasit/applications').respond(apps);
        });

        it('should fail', function(){
            expect(element("settings.applicationName").count()).toBe(3);
        });
    });
});