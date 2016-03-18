var gulp = require('gulp');
var spawn = require('child_process').spawn;
var protractor = require("gulp-protractor").protractor;
var gutil = require('gulp-util');
var mocha = require('gulp-mocha');

var testfolder="./src/test/js/"

gulp.task('unit-test', function () {
	return gulp.src([global.paths.src +'js/basta/**/*_test.js'])
		// gulp-mocha needs filepaths so you can't have any plugins before it 
		.pipe(mocha({reporter: 'min'}));
});



gulp.task('e2e-test', function() {
	return gulp.src([ testfolder +"*.js" ]).pipe(protractor({
		configFile : "./src/test/js/protractor_e2e_test.js"
	})).on('error', function(e) {
		throw e
	})
});

gulp.task('webtest', function() {
	return gulp.src([ testfolder +"*.js" ]).pipe(protractor({
		configFile : "./src/test/js/webtest.js"
	})).on('error', function(e) {
		throw e
	})
});
