'use strict';

var angular = require('angular');

module.exports = ['$location','User', function ($location, User) {

    var vm= this;
    User.onchange(function(){
	 vm.isSuperuser=User.isSuperuser();
    });

    function menuItem(header, description,  icon, url, requireSuperuser){
    	    return {header:header, description:description, icon:icon, url:url, requireSuperuser:requireSuperuser}
    }

    this.menu = [
        menuItem("Noder", 'Virtuelle maskiner', 'redhat.png', '/operations_node'),
        menuItem("Credentials", 'Servicebrukere i AD', 'security.png', '/operations_credential'),
        menuItem("Mq", 'Køer', 'mq.png', '/operations_queue'),
        menuItem("Mq", 'Channel', 'mq.png', '/operations_channel'),
//        menuItem("Database", 'Oracle', 'oracle.png', '/oracle_order'),
    ];

    this.goTo = function (url) {
    	console.log(url)
        $location.url(url);
    };

}];
