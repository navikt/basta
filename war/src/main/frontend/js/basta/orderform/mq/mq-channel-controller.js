'use strict';

module.exports = [ '$http', 'errorService', 'FasitService', 'BastaService', function($http, errorService, FasitService, BastaService) {

	this.data = {
		environmentClass : 'u',
		environmentName : null,
		application : undefined,
		queueManager : undefined,
		fasitAlias : undefined,
		username:undefined,
		mqChannelName : null,
	}

	this.creates = [];

	this.inEditNameMode = false;
	this.inEditAliasMode=false;
	this.inEditUserMode=false;

	var ctrl = this;
	
	this.changeEnvironment = function() {
		this.generateName();
	}
	

	this.changeApplication = function() {
		generateFasitAlias();
		generateAdUserName();
		this.generateName();
	}

	this.changeEnvironmentClass = function() {
		delete this.data.environmentName;
		delete this.queueManager;
		this.generateName();
	}
	

	function generateFasitAlias(){
		if (ctrl.inEditAliasMode) {
			console.log("Will not generate new fasit alias in editmode");
			return;
		}
		ctrl.data.fasitAlias =  ctrl.data.application +"_channel";
	}
	
	function generateAdUserName(){
		if (ctrl.inEditUserMode) {
			//console.log("Will not generate new username in editmode");
			return;
		}
		ctrl.data.username =  "mq"+_.capitalize(ctrl.data.application);
	}

	this.generateName = function() {
		if (this.inEditNameMode) {
			console.log("Will not generate new channelname in editmode");
			return;
		}
		
		var env = '';
		if (ctrl.data.environmentName)
			env = ctrl.data.environmentName.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') + "_";
		var app = '';
		if (ctrl.data.application)
			app = ctrl.data.application.toUpperCase().replace(/-/g, '_').replace(/[^A-Z0-9._]/g, '') ;

		this.data.mqChannelName = env + app ;
	}

	this.submitOrder = function() {
		console.log("Posting mq queue order", this.data)
		BastaService.submitOrderWithUrl('rest/orders/mq/channel', this.data);
	};

} ];
