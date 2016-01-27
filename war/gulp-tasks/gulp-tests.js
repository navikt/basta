var gulp = require('gulp');
var spawn = require('child_process').spawn;
var protractor = require("gulp-protractor").protractor;
var gutil = require('gulp-util');

var child;



gulp.task('runProtractor', function() {
	return gulp.src([ "./src/tests/js/*.js" ]).pipe(protractor({
		configFile : "./src/test/js/protractor_standalone_test.js",
		args : [ '--baseUrl', 'http://localhost:1337' ]
	})).on('error', function(e) {
		throw e
	})
});

gulp.task("e2e-test",['runProtractor'], function() {
	

})