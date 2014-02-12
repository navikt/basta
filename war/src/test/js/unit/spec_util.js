
describe('util', function() {

    it('withObjectInPath', function(){
        withObjectInPath({a : {b : {c: "Hei"}}}, ['a','b','c'], function(object,name){
            expect(object[name]).toBe("Hei");
        });
    });

});