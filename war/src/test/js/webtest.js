// conf file for Jenkins

var seleniumServerJar = require('selenium-server-standalone-jar');
var jasmineReporters = require('jasmine-reporters');

exports.config = {
	capabilities : {
		browserName : 'firefox'
	},
	framework : 'jasmine2',
	seleniumServerJar : seleniumServerJar.path,
	seleniumPort : 2339,
	specs : [ 'scenario/websphere_order_spec.js' ],
	baseUrl: 'http://localhost:1337',

	onPrepare : function() {
		browser.driver.manage().window().maximize();
		browser.get("/");

		var junitReporter = new jasmineReporters.JUnitXmlReporter({
			consolidateAll : true,
			savePath : './target/protractor',
			filePrefix : 'xmloutput',
		});

		jasmine.getEnv().addReporter(junitReporter);
		jasmine.getEnv().addReporter(new jasmineReporters.TapReporter());
	},

//	beforeLaunch : function() {
//		basta.start();
//
//	},
//	onCleanUp : function(exitCode) {
//		console.log("onCleanUp", exitCode);
//		basta.stop();
//	},
//	plugins : [ {
//		package : 'jasmine2-protractor-utils',
//		screenshotOnExpectFailure : true,
//		screenshotOnSpecFailure : true,
//		screenshotPath : "./target/protractor/screenshots/",
//		failTestOnErrorLog : {
//			failTestOnErrorLogLevel : 900,
//		}
//	}, {
//		package : 'protractor-console',
//		logLevels : [ 'severe' ]
//	}
//
//	]

};