var seleniumServerJar = require('selenium-server-standalone-jar');
var jasmineReporters = require('jasmine-reporters');
var SpecReporter = require('jasmine-spec-reporter');

var port = 1337;

exports.config = {
    capabilities: {
        browserName: 'firefox'
    },
    framework: 'jasmine2',
    seleniumServerJar: seleniumServerJar.path,
    seleniumPort: 1339,
    specs: ['scenario/mq_queue_spec.js'],
    baseUrl: 'http://localhost:' + port,
    onPrepare: function () {
        browser.driver.manage().window().setSize(1920, 1200);
        browser.get("/");

        var junitReporter = new jasmineReporters.JUnitXmlReporter({
            consolidateAll: true,
            savePath: './target/protractor',
            filePrefix: 'E2E-webtest-firefox',
        });

        jasmine.getEnv().addReporter(junitReporter);
        jasmine.getEnv().addReporter(new SpecReporter({
            displayStacktrace: 'all'
        }));
    }, plugins: [{
        package: 'jasmine2-protractor-utils',
        screenshotOnExpectFailure: true,
        screenshotOnSpecFailure: true,
        screenshotPath: "./target/protractor/screenshots/",
        clearFoldersBeforeTest: true,
    }]
};