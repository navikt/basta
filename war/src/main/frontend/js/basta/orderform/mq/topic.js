module.exports = Topic;

//public
function Topic(topicString) {
	this.topicString = topicString;
}

Topic.prototype.rightTrunc = function(searchVal) {
	var nextSlash = this.topicString.indexOf("/", searchVal.length);
	if (nextSlash === -1) {
		return this.topicString
	}
	return this.topicString.substring(0, nextSlash);
};

Topic.prototype.matches= function (search){
	return Topic.matches(this.topicString,search);
}

Topic.prototype.mostSignificant= function(){
	return this.topicString.split("/").pop();
}

Topic.prototype.replaceEnvWith= function(environment){
	if(!this.topicString){
		return environment;
	}
	var firstSlash = this.topicString.indexOf("/");
	if(firstSlash === -1){
		return environment;
	}
	return environment + this.topicString.substring(firstSlash);
}

// static
Topic.matches = function(topicString, search){
	return topicString.indexOf(search) === 0;
}

// private
