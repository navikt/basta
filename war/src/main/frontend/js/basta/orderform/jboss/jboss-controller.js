'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", function($scope, User, BastaService) {

    this.choices = {
	memory : [ 512, 1024, 2048, 4096 ],
	serverCount : [ 1, 2, 4 ]
    }
    
    this.settings={
	    classification: {type: 'standard'}
    }

    this.data = {
	nodeType : 'JBOSS',
	environmentClass : 'u',
	zone : 'fss',
	applicationMappingName : null,
	environmentName : null,
	cpuCount : 1,
	serverCount : 1,
	memory : 1024,
	extraDisk : null,
	classification: null
	
    }
    
//    this.alerts={};
    var vm= this;
    
//    function addAlert(alert){
//	vm.alerts[alert]=alert;
//    }
    
    function isCustom(){
//	var cust=vm.data.serverCount > 2;
//	var isCustom=cust;
//	return isCustom;
    }
    
    function isMultiSite(){
	var multiSites=['q0','q1', 'q3','p'];
	return _.contains(multiSites, vm.data.environmentName);
    }
    
    function checkCustom(){
//	if(isCustom()){
//	    vm.settings.classification={type:'custom', lock:true, reason:"Du har valgt mange servere"};
//	}else{
//	    vm.settings.classification={type: 'standard'}
//	}
    }
    
//    addAlert("alert1");
//    addAlert("alert2");
//    addAlert("alert1");

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
//	$scope.form.$setPristine();
    }
    
   
    
    this.checkForCustom = function(){
	checkCustom();
    };

    this.changeEnvironment = function() {
	//checkCustom();
//	console.log(this.data.environmentName)
//	if(isMultiSite()){
//	    addAlert(this.data.environmentName + "er multisite");
//	    this.choices.serverCount=[2,4,6];
//	    if(!_.contains(this.choices.serverCount,this.data.serverCount )){
//		delete this.data.serverCount;
//	    }
//	}
    }

    this.estimatedPrice = function() {
	var unitCost = 600 + 138 + this.data.cpuCount * 100 + this.data.memory * 0.4;
	if(vm.settings.classification.type==='custom'){
	    unitCost=unitCost*2;
	}
	return this.data.serverCount * unitCost;
    }

    this.submitOrder = function() {
	this.data.classification=vm.settings.classification.type;
	this.data.description=vm.settings.classification.description;
	console.log("creating new jboss order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/jboss', this.data);
    };

} ];
