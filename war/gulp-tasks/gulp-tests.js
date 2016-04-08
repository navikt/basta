var gulp = require('gulp');
var spawn = require('child_process').spawn;
var protractor = require("gulp-protractor").protractor;
var gutil = require('gulp-util');
var mocha = require('gulp-mocha');

var unitTests=global.paths.src +'js/basta/**/*_test.js';

gulp.task('unit-test', function () {
	return gulp.src([unitTests])
		// gulp-mocha needs filepaths so you can't have any plugins before it 
		.pipe(mocha({reporter: 'min'}));
});

gulp.task('watch-unit-test', ['unit-test'], function(){
    gulp.watch(global.paths.src +'js/basta/**/*.js', ['unit-test']);
});



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
