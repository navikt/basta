'use strict';

angular.module('skyBestApp.error_controller', [])
  .controller('errorController', ['$scope', function($scope) {

    function getField(object, fields) {
      if (object == null || fields.length == 0) 
        return object;
      else {
        var name = fields[0];
        fields.shift();
        return getField(object[name], fields);
      }
    }

    $scope.isObjectEmpty = function(obj) {
      for(var prop in obj) {
        if(obj.hasOwnProperty(prop))
            return false;
      }
      return true;
    };
    
    $scope.errors = {};

    $scope.$on('GeneralError', function(msg, error) {
      if (error.removeName) {
        if ($scope.errors[error.removeName]) {
          delete $scope.errors[error.removeName];
        }
      } else {
        var message = "";
        if (error.httpError) {
          // http error arguments: data, status, headers, config
          message += 'Feil oppst�tt! Http-kode ' + error.httpError.status;
          var data = error.httpError.data;
          if (_.isString(data) && data.indexOf('<') == 0) {
            data = new X2JS().xml_str2json(data);
            var detailedMessage = getField(data, ['html', 'head', 'title']);
            if (detailedMessage)
              message += ', melding "' + detailedMessage + '". ';
            else 
              message += ' ';
          }
        }
        if (error.message)
          message += error.message + ' ';
        $scope.errors[error.name] = message;
      }
    });
  
  }]);