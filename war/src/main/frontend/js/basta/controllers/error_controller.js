'use strict';

var util = require('../utils/util');
module.exports = ['$rootScope','$scope', function($rootScope, $scope) {

    function getField(object, fields) {
      if (object === null || fields.length === 0) 
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

    $scope.$on('resetAllErrors', function(){
        console.log('resetting error');
        $scope.errors = {};
    });


    $rootScope.$on("$locationChangeStart",function(event, next, current){
       $scope.errors = {};
    });


    $scope.$on('GeneralError', function(msg, error) {
//	console.log("Error", msg, error)
      if (error.removeName) {
        if ($scope.errors[error.removeName]) {
          delete $scope.errors[error.removeName];
        }
      } else {
        var message = "";
        var description="";
        if (error.httpError) {
          // http error arguments: data, status, headers, config

          message += 'Feil oppstått! Http-kode ' + error.httpError.status;

          var data = error.httpError.data;
            //HTML
          if (_.isString(data) && data.indexOf('<') === 0) {      //TODO proper check for HTML
            data = util.xmlToJsonRaw(data);
            var detailedMessage = getField(data, ['html', 'head', 'title']);
            if (detailedMessage)
                description += detailedMessage;
            else
              description += ' ';
          } else if (_.isString(data) && error.httpError.headers('content-type') === 'text/plain'){
              description += data;
          } else if (_.isEmpty(data) && error.httpError.status === 401){
              message += ' (Not authorized)';
          }
        }
        if (error.message)
          message += error.message + ' ';

        $scope.errors[error.name] = {message : message , description: description};
      }
    });
  
  }];