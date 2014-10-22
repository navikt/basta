// conf.js
exports.config = {
    onPrepare: function() {
      require('jasmine-reporters');
       //new reporters.JUnitXmlReporter('reports');
       jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter('reports',true,true));
       jasmine.getEnv().addReporter(new jasmine.TapReporter());

    },
    chromeDriver: 'C:/Users/j116592/AppData/Roaming/npm/node_modules/protractor/selenium/chromedriver.exe',
    seleniumServerJar: 'C:/Users/j116592/AppData/Roaming/npm/node_modules/protractor/selenium/selenium-server-standalone-2.43.1.jar',
    seleniumPort:1339,
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    baseUrl: 'https://basta.adeo.no',
    specs: ['basta_smoke_spec.js']


};

