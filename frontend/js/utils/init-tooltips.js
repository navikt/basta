$(function () {
    $("[data-toggle='popover']").popover();
});

$('.popper').popover({
    container: 'body',
    html: true,
    content: function () {
        var content = $(this).next('.popper-content');
        return content.html();
    }
});

$(function () {
    $("[data-toggle='tooltip']").tooltip({
        html: true,
        title: function () {
            var content = $(this).next('.tooltip-content');
            return content.html();
        }
    })
});

