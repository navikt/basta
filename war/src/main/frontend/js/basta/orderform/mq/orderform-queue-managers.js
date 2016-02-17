'use strict';

module.exports = [ 'FasitService', function(FasitService) {
	
	 // Trick to always get an array. Xml2json will make one item arrays into an object
    function toArray(obj) {
        return [].concat(obj);
    }

	function extractAliases(values) {
		return _.chain(toArray(values))
			.map(function(value) {
				return value.alias;
			})
			.uniq()
			.value();
	}
	
	function extractUsedBy(values) {
		return _.chain(toArray(values))
			.map(function(value) {
				return value.usedby;
			})
			.flatten()
			.uniq()
			.value();
	}

	function transformQM(qmList) {
//		console.log(qmList)
		return _.chain(toArray(qmList))
		.groupBy("url")
		.map(function(values, key) {
			var first=values[0];
//			console.log(first.hostname + ":" + first.port);
			return {
				'url' : key,
				'alias' :extractAliases(values),
				'name' : first.name,
				'hostname' : first.hostname,
				'port' : first.port,
				'usedby' : extractUsedBy(values),
			}
		}).value();
	}

	return {
		restrict : 'E',
		scope : {
			model : '=',
			showValidation : "=",
			onSelect : '&',
			envClassKey : '=',
			application : '='
		},
		controller : [ "$scope", function($scope) {
			require('../../utils/util').initTooltips();
			var ctrl = this;
			
			FasitService.queueManagers().then(function(data) {
				ctrl.choices=[];
				_.each(data, function(value, key) {
					ctrl.choices[key]= transformQM(data[key]);
				});
			});

			function setBestGuess() {
				var choicesForEnv=ctrl.choices[ctrl.envClassKey];
				var bestGuess = choicesForEnv.filter(function(qm) {
					return ctrl.isUsedByApplication(qm);
				});
				if (bestGuess.length > 0) {
					// picking the one that is used before
					ctrl.model = bestGuess[0].url;
				}else{
					//picking a random one from the standard
					ctrl.model=_.chain(choicesForEnv)
						.filter(function(qm){
							return qm.alias.join(",").indexOf("mqGateway")!=-1 
						})
						.shuffle()
						.first()
						.value().url;
				}

			}

			this.orderByUsed = function(qm) {
				if (ctrl.isUsedByApplication(qm)) {
					return 1;
				}
				return 100;
			}

			this.isUsedByApplication = function(qm) {
				return qm && qm.usedby.indexOf(ctrl.application) != -1;
			}
			
			this.isUsedByOtherQueueMananger = function(qm) {
				if(!qm){
					return false;
				}
				var choicesForEnv=ctrl.choices[ctrl.envClassKey];
				var selected=_.find(choicesForEnv, function(choice){
					return choice.url===qm;
				})
				if(ctrl.isUsedByApplication(selected)){
					return false;
				}
				
				var used_by_other= _.find(choicesForEnv, function(choice){
					return _.contains(choice.usedby, ctrl.application)
				});
				
				if(_.isUndefined(used_by_other)){
					return false
				}
//				console.log(ctrl.application ,"is allready using qm", used_by_other.url);
				return true;
			}
			

			$scope.$on("UpdateQueueManangerEvent", function(event, e) {
				// console.log("event", e);
				setBestGuess();
			})

		} ],
		controllerAs : 'ctrl',
		bindToController : true,
		templateUrl : "basta/orderform/mq/orderform-queue-managers.html"
	};
} ];
