'use strict';

var angular = require('angular');

module.exports = [ '$scope', 'User', "BastaService", function($scope, User, BastaService) {

    this.choices = {
	memory : [ 512, 1024, 2048, 4096 ],
	serverCount : [ 1, 2, 4 ]
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
	disk : null,
	classification: {type: 'standard'
	}
    }
    
    this.alerts={};
    var vm= this;
    
    function addAlert(alert){
	vm.alerts[alert]=alert;
    }
    
    addAlert("alert1");
    addAlert("alert2");
    addAlert("alert1");

    this.changeEnvironmentClass = function() {
	delete this.data.environmentName;
//	$scope.form.$setPristine();
    }
    

    this.changeEnvironment = function() {
	console.log(this.data.environmentName)
	addAlert(this.data.environmentName + "er multisite");
	if(this.data.environmentName === 't1'){
	    this.choices.serverCount=[2,4,6];
	    if(!_.contains(this.choices.serverCount,this.data.serverCount )){
		delete this.data.serverCount;
	    }
	}
    }

    this.estimatedPrice = function() {
	var unitCost = 600 + 138 + this.data.cpuCount * 100 + this.data.memory * 0.4;
	return this.data.serverCount * unitCost;
    }

    this.submitOrder = function() {
	console.log("creating new jboss order", this.data);
	BastaService.submitOrderWithUrl('rest/vm/orders/jboss', this.data);
    };

} ];
