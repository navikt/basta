var expect = require('chai').expect;
var Topic = require('./topic');

var topic = new Topic("foo/bar/baz")

describe('TopicString', function() {
	describe('properties', function() {
		it('should return correct value', function() {
			expect(topic.topicString).to.equal('foo/bar/baz');

		});
	});

	describe('topicString', function() {
		it('rightTrunc', function() {
			expect(topic.rightTrunc('fo')).to.equal('foo');
			expect(topic.rightTrunc('foo/')).to.equal('foo/bar');
			expect(topic.rightTrunc('foo/bar')).to.equal('foo/bar');
			expect(topic.rightTrunc('foo/bar/')).to.equal('foo/bar/baz');
			expect(topic.rightTrunc('foo/bar/ba')).to.equal('foo/bar/baz');

		});

		it('matches', function() {
			expect(topic.matches('fo')).to.be.ok;
			expect(topic.matches('nnop')).to.not.be.ok;
		});
		
		it('replaceEnv', function() {
			expect(topic.replaceEnvWith('zoo')).to.equal('zoo/bar/baz');
			expect(new Topic("foo/").replaceEnvWith('zoo')).to.equal('zoo/');
			expect(new Topic("foo").replaceEnvWith('zoo')).to.equal('zoo');
			expect(new Topic("foo/bar").replaceEnvWith('zoo-coo')).to.equal('zoo-coo/bar');
			expect(new Topic().replaceEnvWith('zoo')).to.equal('zoo');
		});

	});

	describe('generator', function() {
		it('generate queue name', function() {
			expect(topic.topicStringWithOutEnv('foo')).to.equal('bar/baz');
			expect(topic.topicStringWithOutEnv('voo')).to.equal('foo/bar/baz');
		});

	});
	
	
});
