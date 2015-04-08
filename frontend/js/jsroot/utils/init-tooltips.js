var $ = require('jquery');
$(function () {
    $("[data-toggle='tooltip']").tooltip({
        html: true,
        title: function () {
            var content = $(this).next('.tooltip-content');
            return content.html();
        }
    })
});

