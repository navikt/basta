'use strict';

var OrderListPage = function (baseUrl) {
    browser.get(baseUrl+'/#/order_list');
};

OrderListPage.prototype = Object.create({}, {
    title:{ get: function() { return browser.getTitle();}},
    orderlist: { get: function () { return element.all(by.repeater('order in orders')); }},
    firstorder: { get: function () {

        return this.orderlist.first().then(function (row) {
            var rowElements = row.all(by.tagName('td'));
            return rowElements.then(function (columns) {
                var col =  columns[0];

                return col.getText();
            });
        });
    }},


    clickFirstOrder: { value: function () {
        return this.orderlist.first().$('a').click();
    }},

    todoText: { get: function () { return element(by.model('todoText')); }},
    addButton: { get: function () { return element(by.css('[value="add"]')); }},
    yourName: { get: function () { return element(by.model('yourName')); }},
    greeting: { get: function () { return element(by.binding('yourName')).getText(); }},

    typeName: { value: function (keys) { return this.yourName.sendKeys(keys); }},
    todoAt: { value: function (idx) { return this.todoList.get(idx).getText(); }},
    addTodo: { value: function (todo) {
        this.todoText.sendKeys(todo);
        this.addButton.click();
    }}
});

module.exports = OrderListPage;