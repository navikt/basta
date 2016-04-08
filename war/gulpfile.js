//process.env.BROWSERIFYSHIM_DIAGNOSTICS=1; //enable denne for å debugge browserify-shim

var gulp = require('gulp');
var del = require('del');
var runSequence = require('run-sequence');
var requireDir = require('require-dir');

//paths for the gulp tasks
global.paths ={
    src: "./src/main/frontend/",
    dest:"./src/main/webapp/",
    env: "development"
};

requireDir('./gulp-tasks');

gulp.task('default', ['watch-js','watch-css', 'watch-files', 'watch-unit-test', 'clean-build']);

gulp.task('build', ['build-js','build-css','copy-files']);

gulp.task('clean', ['clean-js','clean-css','clean-files']);

gulp.task('dist', function () {
    global.paths.env = 'production';
    runSequence('clean', 'build');
});

gulp.task('clean-build', function () {
    runSequence('clean', 'build');
});