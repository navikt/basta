var gulp = require('gulp');
var source = require('vinyl-source-stream');
var del = require('del');
var size = require('gulp-size');
var minifyCSS = require('gulp-minify-css');
var concat = require('gulp-concat');

var paths = {
    css: global.paths.src + 'css/*.css',
    extCss: global.paths.src +'ext/*.css'
}
gulp.task('build-css', function () {
    return gulp.src([
            paths.extCss,
            paths.css,
            './node_modules/font-awesome/css/font-awesome.css',
            './node_modules/angular-hotkeys/build/hotkeys.css'])
        .pipe(concat('bundle.css'))
        .pipe(minifyCSS())
        .pipe(size())
        .pipe(gulp.dest(global.paths.build + 'css'));
});

gulp.task('watch-css', function () {
    gulp.watch(paths.css, ['build-css']);
});

