'use strict';



var util = {};
module.exports = util;


util.withObjectInPath = function(object, path, f) {
      var o = _.chain(path).initial().reduce(function(memo, path) { return memo[path]; }, object).value();
      f(o, _.last(path));
};


util.xmlToJsonRaw = function(data){
    var X2JS = require("./xml2json");
    return new X2JS().xml_str2json(data);
};


util.initTooltips = function(){
    var $ = require('jquery')
    $("[data-toggle='tooltip']").tooltip({
        html: true,
        title: function () {
            var content = $(this).next('.tooltip-content');
            return content.html();
        }
    })
};

function exportUndercoreGlobal(){
    var _ = require('underscore');
    var s = require('underscore.string');
    _.mixin(s.exports());
    _.mixin({arrayify: function(object) {return _.isArray(object) ? object : [object];}});
    window._ = _;
}

function exportJQueryGlobal(){
    var $ = require('jquery');
    window.jQuery = $;
    window.$ = $;
}





exportUndercoreGlobal();
exportJQueryGlobal();











