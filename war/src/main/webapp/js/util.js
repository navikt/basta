'use strict'; 

function withObjectInPath(object, path, f) {
      var o = _.chain(path).initial().reduce(function(memo, path) { return memo[path]; }, object).value();
      f(o, _.last(path)); 
}


_.mixin({
    arrayify: function(object) {
        return _.isArray(object) ? object : [object];
    }
});


