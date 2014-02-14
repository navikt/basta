module.exports = function(config){
    config.set({

   urlRoot: '__karma__',

    basePath : '../',

    files : [
        'js/e2e/*.js'
    ],

    //logLevel : LOG_DEBUG,

    autoWatch : true,

    browsers : ['Chrome'],

    frameworks: ['ng-scenario'],

    singleRun : false,

    proxies : {
      '/': 'http://localhost:1337/',
      '/api/helper/fasit/applications' :'http://localhost:8000/'

    },

    plugins : [
            'karma-junit-reporter',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-jasmine',
            'karma-ng-scenario'
            ],

    junitReporter : {
      outputFile: 'test_out/e2e.xml',
      suite: 'e2e'
    }

})}

