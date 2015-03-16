'use strict';

angular.module('basta.orderform-main-controller', [])
    .controller('orderformMainController', function () {


      this.after = function(){
          console.log("HEI HEI");

        };

        this.data={
            envClass: 'u',
            zone:'fss'
        }

        this.changeEnvironmentClass = function(){
            console.log(this.data);
            if (this.data.envClass === 'u'){
                this.data.zone = 'fss';
            }
        }


    });

