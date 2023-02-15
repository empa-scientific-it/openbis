define([ "require", "stjs" ], function(require, stjs) {
	var FastDownloadSession = function() {
	};
	stjs.extend(FastDownloadSession,  null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.fastdownload.FastDownloadSession';
		constructor.serialVersionUID = 1;
		prototype.downloadUrl = null;
		prototype.fileTransferUserSessionId = null;
		prototype.files = [];
		prototype.options = null;
		prototype.getDownloadUrl = function() {
			return this.downloadUrl;
		};
		prototype.getFileTransferUserSessionId = function() {
			return this.fileTransferUserSessionId;
		};
		prototype.getFiles = function() {
			return this.files;
		};
		prototype.getOptions = function() {
			return this.options;
		};
	}, {
		files : {
			name : "List",
			arguments : [ "IDataSetFileId" ]
		},
		options : "FastDownloadSessionOptions"
	});
	return FastDownloadSession;
})
