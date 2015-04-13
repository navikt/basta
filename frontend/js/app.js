'use strict';

function exposeJqueryGlobal(){
    var $ = require('jquery');
    window.jQuery = $;
    window.$ = $;
    require('bootstrap');
};


function boostrapBasta(){
    require('angular').module('basta',
        [require('angular-route'), require('angular-sanitize'), require('angular-resource'), 'cfp.hotkeys', 'ui.select']
    );
    require('./basta');
};


exposeJqueryGlobal();
boostrapBasta();


