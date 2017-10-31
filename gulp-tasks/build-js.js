var gulp = require('gulp');
var buffer = require('vinyl-buffer');
var source = require('vinyl-source-stream');
var browserify = require('browserify');
var uglify = require('gulp-uglify');
var gulpif = require('gulp-if');
var size = require('gulp-size');
var concat = require('gulp-concat');
var templateCache = require('gulp-angular-templatecache');
var streamqueue = require('streamqueue');
var watchify = require('watchify');
var assign = require('lodash.assign');
var gutil = require('gulp-util');
var del = require('del');


var partials = global.paths.src + 'js/**/*.html';
var appJs = global.paths.src + 'js/app.js'
var dest_js=global.paths.dest+'js';

var customOpts = {
    entries: [appJs],
    debug: true
};

gulp.task('build-js', function(){
    bundle();

});

gulp.task('watch-jsroot', function(){
    var w = watchify(b);
    w.on('update',bundle)
    w.on('log', gutil.log);
});

gulp.task('watch-partials', function(){
    gulp.watch(partials, bundle);
});

gulp.task('watch-js',['watch-jsroot', 'watch-partials']);

gulp.task('clean-js', function(){
    return del.sync([dest_js]);
});


var b = browserify(assign({}, watchify.args, customOpts));
function bundle() {
    var jsStream = b.bundle()
        .on('error', gutil.log.bind(gutil, 'Browserify Error'))
        .pipe(source('tmp'));
    var htmlStream = gulp.src(partials)
        .pipe(templateCache('tmp', {module:'basta'}));

    return  streamqueue({objectMode:true}, jsStream, htmlStream)
        .pipe(buffer())
        .pipe(concat('basta.js'))
        .pipe(gulpif(global.paths.env === 'production', uglify()))
        .pipe(size())
        .pipe(gulp.dest(dest_js));
}
