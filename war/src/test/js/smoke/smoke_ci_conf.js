// conf file for Jenkins
exports.config = {

    capabilities: {
        'browserName': 'firefox'
    },
    seleniumServerJar: '/opt/karma/node_modules/protractor/selenium/selenium-server-standalone-2.43.1.jar',
    seleniumPort:1339,
    baseUrl: 'https://basta.adeo.no',
    specs: ['basta_smoke_spec.js']

};