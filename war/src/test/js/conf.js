// conf file for Jenkins

var seleniumServerJar = process.env['SELENIUM_SERVER_JAR'] || "C:/Users/" + process.env['USERNAME'] + "/AppData/Roaming/npm/node_modules/protractor/selenium/selenium-server-standalone-2.45.0.jar";
var baseUrl = process.env['SMOKETEST_BASEURL'] || "http://localhost:1337";

console.log("Using selenium server jar", seleniumServerJar);
console.log("Using base url", baseUrl);

exports.config = {
    onPrepare: function () {
//        require('jasmine-reporters');
//        jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter('target/reports', true, true));
//        jasmine.getEnv().addReporter(new jasmine.TapReporter());
    },

    capabilities: {
        'browserName': 'firefox'
    },

    seleniumServerJar: seleniumServerJar,
    seleniumPort: 1339,
    baseUrl: baseUrl,
    specs: ['scenario/jboss_order_spec.js']
};