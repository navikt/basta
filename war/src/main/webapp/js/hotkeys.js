(function () {
    function freeKeyOf(c) {
        return {
            combi: c,
            disableInInput: true
        };
    }

    $(document).bind('keyup', freeKeyOf('i'), function () {
        $('#loginForm').removeClass('ng-hide');
        $('#currentUser').addClass('ng-hide');
        $('#loginBtn').addClass('ng-hide');
        $('#usernameInput').focus();
    });

})();
