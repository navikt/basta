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



var src ="./frontend/"
var build="./war/src/main/webapp/"
var resources = "./war/src/main/resources/";

var paths = {
    js: [src+ 'js/*.js', src + 'js/**/*'],
    changelogs: src + 'changelog/**/*',
    css: src + 'css/*.css',
    fonts: [src + 'fonts/**/*', './node_modules/font-awesome/fonts/**/*'],
    extCss: src +'ext/*.css',
    img: src + 'img/**/*',
    favicon: src + 'favicon.ico',
    indexHtml: [src + 'index.html', src+ 'version', src+ 'loginfailure', src + 'loginsuccess'],
    partials: src+ 'partials/**/*.html',
    webInf: resources +'WEB-INF/web.xml',

    buildDir: build,
    jsBuild: build + 'js',
    cssBuild: build + 'css',
    imgBuild: build + 'img',
    fontsBuild: build + 'fonts',
    changlogsBuild: build + 'changelog',
    partialsBuild: build + 'partials',
    webInfBuild: build + 'WEB-INF'

}

//var env = 'production'
var env = 'dev'

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
    return gulp.src([paths.extCss, paths.css,'./node_modules/font-awesome/css/font-awesome.css', './node_modules/angular-hotkeys/build/hotkeys.css' ])
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


gulp.task('copy-webxml', function () {
    return gulp.src(paths.webInf)
        .pipe(gulp.dest(paths.webInfBuild));
});

gulp.task('copy-favicon', function() {
    return gulp.src(paths.favicon).pipe(gulp.dest(paths.buildDir));
})

gulp.task('copy-changelogs', function() {
    return gulp.src(paths.changelogs).pipe(gulp.dest(paths.changlogsBuild));
});

gulp.task('copy-js', function() {
    return gulp.src(paths.js).pipe(gulp.dest(paths.jsBuild));
});

gulp.task('copy-partials', function() {
    return gulp.src(paths.partials).pipe(gulp.dest(paths.partialsBuild));
});

gulp.task('copy-img', function() {
    return gulp.src(paths.img).pipe(gulp.dest(paths.imgBuild));
});


gulp.task('watch', function () {
    gulp.watch(paths.js, ['compile-js']);
    gulp.watch(paths.css, ['bundle-css']);
    gulp.watch(paths.indexHtml, ['copy-indexhtml']);
    gulp.watch(paths.partials, ['copy-partials']);
});


gulp.task('clean', function (cb) {
    return del(paths.buildDir, cb);
});

gulp.task('default', ['watch', 'clean-build']);

gulp.task('clean-build', function () {
    runSequence('clean', 'build');
});

gulp.task('build', ['compile-js', 'bundle-css', 'copy-fonts', 'copy-indexhtml', 'copy-favicon', 'copy-changelogs', 'copy-partials', 'copy-webxml', 'copy-img']);

gulp.task('dist', function () {
    env = 'production';
    runSequence('clean', 'build');
});