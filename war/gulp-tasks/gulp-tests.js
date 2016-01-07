var gulp = require('gulp');
var spawn = require('child_process').spawn;
var protractor = require("gulp-protractor").protractor;
var gutil = require('gulp-util');

var child;

gulp.task("start:server", function(){
    child = spawn("mvn", ["exec:java", "-Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner","-Dexec.classpathScope=test"], {cwd: process.cwd()}),
        stdout = '',
        stderr = '';
	
//	child = spawn("ls", ["-la"], {cwd: process.cwd()}),
//  stdout = '',
//  stderr = '';

    child.stdout.setEncoding('utf8');

    child.stdout.on('data', function (data) {
        stdout += data;
        gutil.log(data);
    });

    child.stderr.setEncoding('utf8');
    child.stderr.on('data', function (data) {
        stderr += data;
        gutil.log(gutil.colors.red(data));
        gutil.beep();
    });

    child.on('close', function(code) {
        gutil.log("Done with exit code", code);
        gutil.log("You access complete stdout and stderr from here"); // stdout, stderr
    });

	
});

gulp.task('runProtractor', function() {
	return gulp.src([ "./src/tests/js/*.js" ]).pipe(protractor({
		configFile : "./src/test/js/protractor.js",
		args : [ '--baseUrl', 'http://localhost:1337' ]
	})).on('error', function(e) {
		throw e
	})
	
	
});

gulp.task("test",['runProtractor'], function() {
	

})