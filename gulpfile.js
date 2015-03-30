var gulp = require('gulp');
var buffer = require('vinyl-buffer');
var browserify = require('browserify');
var source = require('vinyl-source-stream');
var uglify = require('gulp-uglify');
var rename = require('gulp-rename');
var gulpif = require('gulp-if');
var size = require('gulp-size');
var minifyCSS = require('gulp-minify-css');
var concat = require('gulp-concat');
var del = require('del');
var runSequence = require('run-sequence');
//var templateCache = require('gulp-angular-templatecache')


var src ="./frontend/"
var build="./war/src/main/webapp/"





var paths = {
    js: [src+ 'js/*.js', src + 'js/**/*'],
    jsLibs: src + 'lib/**/*',
    css: src + 'css/*.css',
    fonts: [src + 'lib/fonts/*.*', './node_modules/font-awesome/fonts/**/*'],
    extCss: src + 'lib/bootstrap-yeti/*.css',
    favicon: src + 'favicon.ico',
    indexHtml: src + 'index.html',
    partials: src+ 'partials/**/*.html',

    buildDir: build,
    jsBuild: build + 'js',
    cssBuild: build + 'css',
    fontsBuild: build + 'fonts',
    libsBuild: build + 'lib',
    partialsBuild: build + 'partials'

}

var env = 'production'

gulp.task('compile-js', function () {
    return browserify(src +'js/app.js')
        .bundle()
        .pipe(source('basta.js'))
        .pipe(buffer())
        .pipe(gulpif(env === 'production', uglify()))
        .pipe(size())
        .pipe(gulp.dest(paths.jsBuild));
});

gulp.task('bundle-css', function () {
    return gulp.src(['./node_modules/font-awesome/css/font-awesome.css',paths.extCss, paths.css ])
        .pipe(concat('bundle.css'))
        .pipe(minifyCSS())
        .pipe(size())
        .pipe(gulp.dest(paths.cssBuild));
});

gulp.task('copy-fonts', function () {
    return gulp.src(paths.fonts)
        .pipe(gulp.dest(paths.fontsBuild));
});

gulp.task('copy-indexhtml', function () {
    return gulp.src(paths.indexHtml)
        .pipe(gulp.dest(paths.buildDir));
});

gulp.task('copy-favicon', function() {
    return gulp.src(paths.favicon).pipe(gulp.dest(paths.buildDir));
})

gulp.task('copy-libs', function() {
    return gulp.src(paths.jsLibs).pipe(gulp.dest(paths.libsBuild));
});

gulp.task('copy-js', function() {
    return gulp.src(paths.js).pipe(gulp.dest(paths.jsBuild));
});

gulp.task('copy-partials', function() {
    return gulp.src(paths.partials).pipe(gulp.dest(paths.partialsBuild));
});


gulp.task('watch', function () {
    gulp.watch(paths.js, ['compile-js']);
    gulp.watch(paths.css, ['bundle-css']);
    gulp.watch(paths.indexHtml, ['copy-indexhtml']);
});


gulp.task('clean', function (cb) {
    return del(paths.buildDir, cb);
});

gulp.task('default', ['watch', 'clean-build']);

gulp.task('clean-build', function () {
    runSequence('clean', 'build');
});

gulp.task('build', ['compile-js', 'bundle-css', 'copy-fonts', 'copy-indexhtml', 'copy-favicon', 'copy-libs', 'copy-js', 'copy-partials']);

gulp.task('dist', function () {
    env = 'production';
    runSequence('clean', 'build');
});