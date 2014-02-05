describe('bestSkyApp', function() {
    describe('Order View', function() {
        beforeEach(function() {
            browser().navigateTo('../index.html');
        });
        it('should display orders list when accessing index.html', function() {
            expect(browser().location().url()).toBe('/order_list')
        });
    });
});