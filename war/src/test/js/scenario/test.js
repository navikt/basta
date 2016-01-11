'use strict';


describe('Dummy test', function () {

    it('Tittel på maven.adeo.no', function () {
    	 it('should have a title', function() {
    		    browser.get('http://maven.adeo.no/');
    		    expect(browser.getTitle()).toEqual('maven.adeo.no');
    		  });

    });
    
});