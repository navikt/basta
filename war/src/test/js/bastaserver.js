var spawn = require('child_process').spawn;
var kill = require('tree-kill');

var server;
var logPrefix = "[bastaServer]"

function log(message) {
    console.log(logPrefix, new Date(), message);
}

function start(port) {
    var mvn = process.platform === "win32" ? "mvn.cmd" : "mvn";
    var options = {
        detached: false
    }
    server = spawn(mvn, ["exec:java", "-Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner", "-Dexec.classpathScope=test", "-Dport=" + port], options);

    server.stdout.on('data', function (data) {
        if (data.indexOf("Jetty started on port") > -1) {
            log("################### Started jetty");
        }
        log('stdout: ' + data);
    });

    server.stderr.on('data', function (data) {
        log('stdout: ' + data);

    });

    log("Starting server with pid " + server.pid)
    var time = 20;
    var stop = new Date().getTime();
    while (new Date().getTime() < stop + time * 1000) {
    }
    return server;
}

function stop() {
    log("Stopping server with pid", server.pid)
    kill(server.pid);
}

module.exports = {
    start: start,
    stop: stop,
}
