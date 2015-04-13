module.exports =  function () {
    return {
        hasEnvironmentClassAccess: function ($scope, environmentClass) {
            if ($scope.currentUser) {
                var classes = $scope.currentUser.environmentClasses;
                return classes.indexOf(environmentClass) > -1;
            }
            return false;
        },
        hasEnvClassAccess: function (environmentClass, currentUser) {
            var classes = currentUser.environmentClasses;
            return classes.indexOf(environmentClass) > -1;

        },

        isLoggedIn: function (user) {
            return (!_.isUndefined(user) && user.authenticated);
        }
    }
};

