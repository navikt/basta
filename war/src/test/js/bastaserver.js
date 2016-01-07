var spawn = require('child_process').spawn;
var exec = require('child_process').exec;

var child;

var logPrefix="[bastaServer]"
	
function log(message){
	console.log(logPrefix,message);
}	

function start() {
	// child = spawn("/c/apps/apache-maven-3.3.1/bin/mvn", [ "exec:java",
	// "-Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner",
	// "-Dexec.classpathScope=test" ], { cwd : process.cwd() , env:
	// process.env}),
//	console.log( process.env.PATH );
//	 child = spawn("mvn", ["--version" ], { cwd : process.cwd() , env: process.env});
	child = exec('mvn exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec.classpathScope=test ', function(error, stdout,
			stderr) {
		log(stdout);
		log(stderr);
		if (error !== null) {
			log("exec error:"+ error);
		}

	});
	console.log("Startet server with pid", child.pid)
//	 console.log(child);

	child.stdout.setEncoding('utf8');
	child.stdout.on('data', function(data) {
		var str = data.toString()
		var lines = str.split(/(\r?\n)/g);
		log(lines.join(""));

	});

	child.stderr.setEncoding('utf8');
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
	child.kill();
}

module.exports = {
	start : start,
	stop : stop
}
