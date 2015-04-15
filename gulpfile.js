var gulp = require('gulp');
var del = require('del');
var runSequence = require('run-sequence');
var requireDir = require('require-dir');

//paths for the gulp tasks
global.paths ={
    src: "./frontend",
    build:"./war/src/main/webapp/",
    env: "development"
}

requireDir('./gulp-tasks');

gulp.task('default', ['watch-js', 'watch-css', 'watch-files','clean-build']);

gulp.task('build', ['build-js','build-css','copy-files']);

gulp.task('clean', function (cb) {
    return del(global.paths.build, cb);
});

gulp.task('dist', function () {
    global.paths.env = 'production';
    runSequence('clean', 'build');
});

gulp.task('clean-build', function () {
    runSequence('clean', 'build');
});