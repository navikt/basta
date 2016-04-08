// conf file for Jenkins

var seleniumServerJar = require('selenium-server-standalone-jar');
var basta = require("./bastaserver");
var jasmineReporters = require('jasmine-reporters');

var port=9937

exports.config = {
	capabilities : {
		browserName : 'firefox'
	},
	framework : 'jasmine2',
	seleniumServerJar : seleniumServerJar.path,
	seleniumPort : 1339,
	specs : [ 'scenario/*_spec.js' ],
	baseUrl: 'http://localhost:' + port,

	onPrepare : function() {
		browser.driver.manage().window().setSize(1920, 1200);
		browser.get("/");

		var junitReporter = new jasmineReporters.JUnitXmlReporter({
			consolidateAll : true,
			savePath : './target/protractor',
			filePrefix : 'E2E-webtest-firefox',
		});

		jasmine.getEnv().addReporter(junitReporter);
		jasmine.getEnv().addReporter(new jasmineReporters.TapReporter());
	},

	beforeLaunch : function() {
		basta.start(port);

	},
	onCleanUp : function(exitCode) {
		console.log("onCleanUp", exitCode);
		basta.stop();
	},
	plugins : [ {
		package : 'jasmine2-protractor-utils',
		screenshotOnExpectFailure : true,
		screenshotOnSpecFailure : true,
		screenshotPath : "./target/protractor/screenshots/",
//		failTestOnErrorLog : {
//			failTestOnErrorLogLevel : 900,
//		}
	},
//	{
//		package : 'protractor-console',
//		logLevels : [ 'severe' ]
//	}

	]

};