var spawn = require('child_process').spawn;
var kill = require('tree-kill');

var child;

var logPrefix = "[bastaServer]"

function log(message) {
	console.log(logPrefix, message);
}

function start() {
	var mvn = process.platform === "win32" ? "mvn.cmd" : "mvn";
	var options = {
		detached : true
	}
	child = spawn(mvn, [ "exec:java", "-Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner", "-Dexec.classpathScope=test" ], options);


	child.on('close', function(code) {
		log("Done with exit code " + code);
	});

	child.on('exit', function(code) {
		log("Exit with code " + code);
	});

	// Waiting for 10 sek to start server
	log("Waiting a bit until server has started");
	setTimeout(function() {
		log("Started server with pid " + child.pid);
		}, 10000);
	return child;
}

function stop() {
	log("Stopping server with pid", child.pid)
	kill(child.pid);
}

module.exports = {
	start : start,
	stop : stop
}
