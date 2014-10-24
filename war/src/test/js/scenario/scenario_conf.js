// DOES NOT YET WORK, JUST FOR KEEPSAKE

var chromeDriver = process.env['SELENIUM_CHROMEDRIVER'] || "C:/Users/" + process.env['USERNAME'] + "/AppData/Roaming/npm/node_modules/protractor/selenium/chromedriver.exe";
var seleniumServerJar = process.env['SELENIUM_SERVER_JAR'] || "C:/Users/" + process.env['USERNAME'] + "/AppData/Roaming/npm/node_modules/protractor/selenium/selenium-server-standalone-2.43.1.jar";
var baseUrl = process.env['SMOKETEST_BASEURL'] || "http://localhost:8086";

console.log("Using chromedriver", chromeDriver);
console.log("Using selenium server jar", seleniumServerJar);
console.log("Using base url", baseUrl);

exports.config = {

    onPrepare: function () {
        require('jasmine-reporters');
        jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter('target/reports', true, true));
        jasmine.getEnv().addReporter(new jasmine.TapReporter());
    },

    multiCapabilities: [
        {
            'browserName': 'firefox'
        },
        {
            'browserName': 'chrome'
        }
    ],

    chromeDriver: chromeDriver,
    seleniumServerJar: seleniumServerJar,
    seleniumPort: 1339,
    baseUrl: baseUrl,
    specs: ['basta_smoke_spec.js']

};