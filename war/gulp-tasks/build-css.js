var gulp = require('gulp');
var source = require('vinyl-source-stream');
var del = require('del');
var size = require('gulp-size');
var minify = require('gulp-minify-css');
var concat = require('gulp-concat');

var paths = {
    css: global.paths.src + 'css/*.css',
    extCss: global.paths.src +'ext/*.css',
    hotkeys:'./node_modules/angular-hotkeys/build/hotkeys.css',
    fontawesome: './node_modules/font-awesome/css/font-awesome.css'
}
gulp.task('build-css', function () {
    return gulp.src([paths.extCss, paths.css,paths.hotkeys, paths.fontawesome])
        .pipe(concat('bundle.css'))
        .pipe(minify())
        .pipe(size())
        .pipe(gulp.dest(global.paths.build + 'css'));
});

gulp.task('watch-css', function () {
    gulp.watch(paths.css, ['build-css']);
});
