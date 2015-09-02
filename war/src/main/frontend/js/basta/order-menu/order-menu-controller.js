'use strict';

var angular = require('angular');

module.exports = ['$location', function ($location) {
    
    	function menuItem(header, description,  icon, url, show){
    	    return {header:header, description:description, icon:icon, url:url}
    	}
    
    
    	this.menu=[
    		menuItem("WAS",'Node','websphere.png','/was_node_order'),
    		menuItem("WAS",'Deployment Manager','websphere.png','/was_dmgr_order'),
    		menuItem("BPM",'Node','websphere.png','/bpm_node_order'),
    		menuItem("BPM",'Deployment Manager','websphere.png','/bpm_dmgr_order'),
    		menuItem("JBoss",'Application server','jboss.png','/jboss_order'),
    		menuItem("Red Hat",'Linux','redhat.png','/linux_order'),
    		menuItem("Credentials",'for Service user','security.png','/serviceuser_credential_order'),
    		menuItem("Certificate",'for Service user','security.png','/serviceuser_certificate_order'),
    		menuItem("OpenAM",'Server','openam.png','/openam_order'),
    		menuItem("OpenAM",'Proxy','openam.png','/openam_proxy_order')

    	];

	this.goTo = function(url) {
	    $location.url(url);
	};
	
} ];
