define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ContentCopy = function() {
	};
	stjs.extend(ContentCopy, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.ContentCopy';
		constructor.serialVersionUID = 1;
		prototype.id = null;
		prototype.externalDms = null;
		prototype.externalCode = null;
		prototype.path = null;
		prototype.gitCommitHash = null;
		prototype.gitRepositoryId = null;
		
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
		prototype.getExternalDms = function() {
			return this.externalDms;
		};
		prototype.setExternalDms = function(externalDms) {
			this.externalDms = externalDms;
		};
		prototype.getExternalCode = function() {
			return this.externalCode;
		};
		prototype.setExternalCode = function(externalCode) {
			this.externalCode = externalCode;
		};
		prototype.getPath = function() {
			return this.path;
		};
		prototype.setPath = function(path) {
			this.path = path;
		};
		prototype.getGitCommitHash = function() {
			return this.gitCommitHash;
		};
		prototype.setGitCommitHash = function(gitCommitHash) {
			this.gitCommitHash = gitCommitHash;
		};
		prototype.getGitRepositoryId = function() {
			return this.gitRepositoryId;
		};
		prototype.setGitRepositoryId = function(gitRepositoryId) {
			this.gitRepositoryId = gitRepositoryId;
		};
	}, {
		id : "ContentCopyPermId",
		externalDms : "ExternalDms"
	});
	return ContentCopy;
})
