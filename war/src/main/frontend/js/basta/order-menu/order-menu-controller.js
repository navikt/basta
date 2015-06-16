'use strict';

var angular = require('angular');

module.exports = ['$location', function ($location) {
    
    	function menuItem(header, description,  icon, url, show){
    	    return {header:header, description:description, icon:icon, url:url}
    	}
    
    
    	this.menu=[
    		menuItem("WAS,",'Node','websphere.png','/was_node_order'),
    		menuItem("WAS,",'Deployment Mananger','websphere.png','/vm_order?orderType=WAS_DEPLOYMENT_MANAGER'),
    		menuItem("BPM,",'Node','websphere.png','vm_order?orderType=BPM_NODES'),
    		menuItem("BPM,",'Deployment Mananger','websphere.png','/vm_order?orderType=BPM_DEPLOYMENT_MANAGER'),
    		menuItem("Jboss,",'Application server','jboss.png','/jboss_order'),
    		menuItem("Red hat,",'Linux','redhat.png','/linux_order'),
    		menuItem("Credentials,",'for Service user','security.png','/serviceuser_credential_order'),
    		menuItem("Certificate,",'for Service user','security.png','/serviceuser_certificate_order'),
    		
    	];

	this.goTo = function(url) {
	    $location.url(url);
	};
	
} ];
