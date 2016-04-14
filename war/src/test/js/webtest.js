// conf file for Jenkins

var seleniumServerJar = require('selenium-server-standalone-jar');
var jasmineReporters = require('jasmine-reporters');
var SpecReporter = require('jasmine-spec-reporter');

exports.config = {
	capabilities : {
		browserName : 'firefox'
	},
	framework : 'jasmine2',
	seleniumServerJar : seleniumServerJar.path,
	seleniumPort : 2339,
	specs : [ '*/*_spec.js' ],
	baseUrl : 'http://localhost:1337',
	jasmineNodeOpts : {
		print : function() {
		}
	},

	onPrepare : function() {
		browser.driver.manage().window().maximize();
		browser.get("/");

		var junitReporter = new jasmineReporters.JUnitXmlReporter({
			consolidateAll : true,
			savePath : './target/protractor',
			filePrefix : 'xmloutput',
		});

		// jasmine.getEnv().addReporter(junitReporter);
		// jasmine.getEnv().addReporter(new
		// jasmineReporters.TerminalReporter());
		// jasmine.getEnv().addReporter(new jasmineReporters.TapReporter());
		jasmine.getEnv().addReporter(new SpecReporter({
			displayStacktrace : 'all'
		}));
	},

//	plugins : [ {
//		package : 'jasmine2-protractor-utils',
//		disableHTMLReport : false,
//		disableScreenshot : false,
//		screenshotPath : './target/protractor/screenshots',
//		screenshotOnExpectFailure : true,
//		screenshotOnSpecFailure : true,
//		clearFoldersBeforeTest : true,
//		htmlReportDir : './target/protractor/htmlReports',
////		failTestOnErrorLog : {
////			failTestOnErrorLogLevel : 900,
////			excludeKeywords : [ 'keyword1', 'keyword2' ]
////		}
//
//	}, 
//	{
//		package : 'protractor-console',
//		logLevels : [ 'severe' ]
//	}
//
//	]

};
