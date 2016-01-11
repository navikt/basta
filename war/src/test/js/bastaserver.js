var spawn = require('child_process').spawn;
var kill = require('tree-kill');

var child;

var logPrefix = "[bastaServer]"
	

function log(message) {
	console.log(logPrefix,new Date(),  message);
}

function start() {
	var mvn = process.platform === "win32" ? "mvn.cmd" : "mvn";
	var options = {
		detached : false
	}
	child = spawn(mvn, [ "exec:java", "-Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner", "-Dexec.classpathScope=test" ], options);


	child.on('close', function(code) {
		log("Done with exit code " + code);
	});

	child.on('exit', function(code) {
		log("Exit with code " + code);
	});
	
	child.stdout.on('data', function(data) {
		if(data.indexOf("Jetty started on port")> -1){
			log("################### Started jetty" );
		}
//			    log('stdout: ' + data);
	});
	
	child.stderr.on('data', function(data) {
	    log('stdout: ' + data);
	    
	});
	
	log("Starting server with pid " + child.pid)  
	var time = 20;
	var stop = new Date().getTime();
    while(new Date().getTime() < stop + time*1000) {
    	;
    }
	return child;
}

function stop() {
	log("Stopping server with pid", child.pid)
	kill(child.pid);
}

module.exports = {
	start : start,
	stop : stop,
}
