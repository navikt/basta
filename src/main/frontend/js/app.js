'use strict';

function exposeJqueryGlobal(){
    var $ = require('jquery');
    window.jQuery = $;
    window.$ = $;
    require('bootstrap');
};


function bootstrapBasta(){
    require('angular').module('basta',
        [require('angular-route'), require('angular-sanitize'), require('angular-resource'), 'ngTagsInput','cfp.hotkeys', 'ui.select', require('angular-ui-bootstrap')]
    );
    require('ng-tags-input');
    require('./basta');
};


exposeJqueryGlobal();
bootstrapBasta();


