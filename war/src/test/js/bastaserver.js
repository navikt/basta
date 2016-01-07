var spawn = require('child_process').spawn;
var exec = require('child_process').exec;
var kill = require('tree-kill');

var child;

var logPrefix="[bastaServer]"
	
function log(message){
	console.log(logPrefix,message);
}	

function start() {
	var mvn = process.platform === "win32" ? "mvn.cmd" : "mvn";
	var options={detached :true}
	 child = spawn(mvn, [ "exec:java", "-Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner", "-Dexec.classpathScope=test" ], options);

	console.log("Startet server with pid", child.pid)

	child.stdout.on('data', function(data) {
		var str = data.toString()
		var lines = str.split(/(\r?\n)/g);
		log(lines.join(""));

	});

	child.stderr.on('data', function(data) {
		var str = data.toString()
		var lines = str.split(/(\r?\n)/g);
		log("stderr "+ lines.join(""));
	});

	child.on('close', function(code) {
		log("Done with exit code "+ code);
	});
	
	child.on('exit', function(code) {
		log("Exit with code "+ code);
	});
	
	return child;
}

function stop() {
	console.log("Stopping server with pid", child.pid)
	kill(child.pid);
}

module.exports = {
	start : start,
	stop : stop
}
