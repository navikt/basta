var gulp = require('gulp');

var root

gulp.task('copy-root', function(){
    return gulp.src(global.paths.src +  'root/**/*').pipe(gulp.dest(global.paths.build));
});

gulp.task('copy-fa', function(){
    return gulp.src('./node_modules/font-awesome/fonts/**/*').pipe(gulp.dest(global.paths.build + 'fonts'));
});

gulp.task('watch-files', function () {
    gulp.watch(global.paths.root, ['copy-root']);
});


gulp.task('copy-files',['copy-root', 'copy-fa']);
