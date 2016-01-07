// conf file for Jenkins

var seleniumServerJar = require('selenium-server-standalone-jar');
var basta= require("./bastaserver");

exports.config = {

	onPrepare : function() {
		browser.driver.manage().window().maximize();
		// console.log(baseUrl);
		// require('jasmine-reporters');
		// jasmine.getEnv().addReporter(new
		// jasmine.JUnitXmlReporter('target/reports', true, true));
		// jasmine.getEnv().addReporter(new jasmine.TapReporter());
	},

	capabilities : {
		'browserName' : 'firefox'
	},

	seleniumServerJar : seleniumServerJar.path,
	seleniumPort : 1339,
	specs : [ 'scenario/*.js' ],

	beforeLaunch : function() {
		console.log("BeforeLaunch");
		basta.start();
	
	},
	onCleanUp : function(exitCode) {
		console.log("onCleanUp", exitCode);
		basta.stop();
	},

};