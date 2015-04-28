var gulp = require('gulp');
var del = require('del');

gulp.task('copy-root', function(){
    return gulp.src(global.paths.src +  'root/**/*').pipe(gulp.dest(global.paths.dest));
});

gulp.task('copy-fa', function(){
    return gulp.src('./node_modules/font-awesome/fonts/**/*').pipe(gulp.dest(global.paths.dest + 'fonts'));
});

gulp.task('watch-files', function () {
    gulp.watch(global.paths.src +  'root/**/*', ['copy-root']);
});

gulp.task('clean-files', function(){
    return del.sync([global.paths.dest+ '{changelog,fonts,img,index.html,favicon.ico,login*,version}']);

});

gulp.task('copy-files',['copy-root', 'copy-fa']);
