
describe('util', function() {

    it('withObjectInPath', function(){
        withObjectInPath({a : {b : {c: 'Hei'}}}, ['a','b','c'], function(object,name){
            expect(object[name]).toBe('Hei');
        });
    });


    it('withObjectInPath2', function(){
        withObjectInPath({ a : { b : { c: 'Hei'}}}, ['a','b'], function(object,name){
            expect(object[name].c).toBe('Hei');
        });
    });

    it('should convert a string to an array', function(){
        expect(_.arrayify('a')).toEqual(['a']);

    });

    it('should convert an object to an array', function(){
        expect(_.arrayify({a : "aa", b: "bb"})).toEqual([{a : "aa", b: "bb"}]);

    });

    it('should not convert an array to an array of array', function(){
        expect(_.arrayify(['a'])).toEqual(['a']);
    });

    it('should not convert an array of object to an array of array of object', function(){
        expect(_.arrayify([{a : "aa", b: "bb"}])).toEqual([{a : "aa", b: "bb"}]);
    });


});
