// Karma configuration
// Generated on Wed Feb 05 2014 13:47:03 GMT+0100 (Central Europe Standard Time)

module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '',

    // frameworks to use
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
      '../../main/webapp/lib/jquery-1.10.2.min.js',
      '../../main/webapp/lib/select2-3.4.2/select2.min.js',
      '../../main/webapp/lib/select2-3.4.2/select2_locale_no.js',
      '../../main/webapp/lib/xml2json/xml2json.min.js',
      '../../main/webapp/lib/underscore/underscore-min.js',
      '../../main/webapp/lib/angular/angular.js',
      '../../main/webapp/lib/angular/angular-route.js',
      '../../main/webapp/lib/angular/angular-resource.js',
      '../../main/webapp/lib/bootstrap/js/bootstrap.js',
      '../../main/webapp/lib/angular/ui-bootstrap-tpls-0.6.0.min.js',
      '../../main/webapp/lib/angular/select2.js',
      '../../main/webapp/js/app.js',
      '../../main/webapp/js/util.js',
      '../../main/webapp/js/main_controller.js',
      '../../main/webapp/js/error_controller.js',
      '../../main/webapp/js/order_form_controller.js',
      '../../main/webapp/js/order_list_controller.js',
      '../../main/webapp/js/node_list_controller.js',
      'angular-mocks.js',
      {pattern: '../js/unit/*.js', included: true}
    ],


    // list of files to exclude
    exclude: [

    ],


    // test results reporter to use
    // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
    reporters: ['dots','junit', 'coverage'],

    junitReporter: {outputFile:'../reports/result/TESTS-results.xml'},

    preprocessors: {
          // source files, that you wanna generate coverage for
          // do not include tests or libraries
          // (these files will be instrumented by Istanbul)
          '../../main/webapp/js/*.js': ['coverage']
     },

     // optionally, configure the reporter
      coverageReporter: {
          type : 'lcovonly',
          dir : '../reports/coverage/'
      },

    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera (has to be installed with `npm install karma-opera-launcher`)
    // - Safari (only Mac; has to be installed with `npm install karma-safari-launcher`)
    // - PhantomJS
    // - IE (only Windows; has to be installed with `npm install karma-ie-launcher`)
    browsers: ['PhantomJS'],


    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: true
  });
};
