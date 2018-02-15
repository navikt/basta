var gulp = require('gulp');
var protractor = require("gulp-protractor").protractor;
var mocha = require('gulp-mocha');

var unitTests = global.paths.src + 'js/basta/**/*_test.js';

gulp.task('unit-test', function () {
    // gulp-mocha needs filepaths so you can't have any plugins before it
    return gulp.src([unitTests]).pipe(mocha({reporter: 'min'}));
});

gulp.task('watch-unit-test', ['unit-test'], function () {
    gulp.watch(global.paths.src + 'js/basta/**/*.js', ['unit-test']);
});

gulp.task('e2e-test', function () {
    return gulp.src(["./src/tests/js/*.js"]).pipe(protractor({
        configFile: "./src/test/js/protractor_config.js"
    })).on('error', function (e) {
        throw e
    })
});