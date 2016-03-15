var gulp = require('gulp');
var spawn = require('child_process').spawn;
var protractor = require("gulp-protractor").protractor;
var gutil = require('gulp-util');

var child;



gulp.task('e2e-test', function() {
	return gulp.src([ "./src/tests/js/*.js" ]).pipe(protractor({
		configFile : "./src/test/js/protractor_e2e_test.js"
	})).on('error', function(e) {
		throw e
	})
});

gulp.task('webtest', function() {
	return gulp.src([ "./src/tests/js/*.js" ]).pipe(protractor({
		configFile : "./src/test/js/webtest.js"
	})).on('error', function(e) {
		throw e
	})
});
