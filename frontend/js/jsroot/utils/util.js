'use strict'; 

function withObjectInPath(object, path, f) {
      var o = _.chain(path).initial().reduce(function(memo, path) { return memo[path]; }, object).value();
      f(o, _.last(path)); 
}

function xml2json(data, getter) {
    var contentType = getter()['content-type'];
    if (contentType && contentType.match('application/xml'))
        return new X2JS().xml_str2json(data);
    return {};
}






