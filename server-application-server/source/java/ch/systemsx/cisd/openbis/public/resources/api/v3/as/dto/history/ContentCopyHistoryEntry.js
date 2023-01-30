define([ "stjs", "util/Exceptions", "as/dto/history/HistoryEntry" ], function(stjs, exceptions, HistoryEntry) {
	var ContentCopyHistoryEntry = function() {
		HistoryEntry.call(this);
	};
	stjs.extend(ContentCopyHistoryEntry, HistoryEntry, [ HistoryEntry ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.history.ContentCopyHistoryEntry';
		constructor.serialVersionUID = 1;
		prototype.externalCode = null;
		prototype.path = null;
		prototype.gitCommitHash = null;
		prototype.gitRepositoryId = null;
		prototype.externalDmsId = null;
		prototype.externalDmsCode = null;
		prototype.externalDmsLabel = null;
		prototype.externalDmsAddress = null;

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
		prototype.getExternalDmsId = function() {
			return this.externalDmsId;
		};
		prototype.setExternalDmsId = function(externalDmsId) {
			this.externalDmsId = externalDmsId;
		};
		prototype.getExternalDmsCode = function() {
			return this.externalDmsCode;
		};
		prototype.setExternalDmsCode = function(externalDmsCode) {
			this.externalDmsCode = externalDmsCode;
		};
		prototype.getExternalDmsLabel = function() {
			return this.externalDmsLabel;
		};
		prototype.setExternalDmsLabel = function(externalDmsLabel) {
			this.externalDmsLabel = externalDmsLabel;
		};
		prototype.getExternalDmsAddress = function() {
			return this.externalDmsAddress;
		};
		prototype.setExternalDmsAddress = function(externalDmsAddress) {
			this.externalDmsAddress = externalDmsAddress;
		};
}, {});
	return ContentCopyHistoryEntry;
})