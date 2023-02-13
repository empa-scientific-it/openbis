define([ "stjs" ], function(stjs) {
	var DataSetFileCreation = function() {
	};
	stjs.extend(DataSetFileCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.create.DataSetFileCreation';
		constructor.serialVersionUID = 1;
		prototype.path = null;
		prototype.directory = null;
		prototype.fileLength = null;
		prototype.checksumCRC32 = null;
		prototype.checksum = null;
		prototype.checksumType = null;
		
		prototype.getPath = function() {
			return this.path;
		};
		prototype.setPath = function(path) {
			this.path = path;
		};
		prototype.isDirectory = function() {
			return this.directory;
		};
		prototype.setDirectory = function(directory) {
			this.directory = directory;
		};
		prototype.getFileLength = function() {
			return this.fileLength;
		};
		prototype.setFileLength = function(fileLength) {
			this.fileLength = fileLength;
		};
		prototype.getChecksumCRC32 = function() {
			return this.checksumCRC32;
		};
		prototype.setChecksumCRC32 = function(checksumCRC32) {
			this.checksumCRC32 = checksumCRC32;
		};
		prototype.getChecksum = function() {
			return this.checksum;
		};
		prototype.setChecksum = function(checksum) {
			this.checksum = checksum;
		};
		prototype.getChecksumType = function() {
			return this.checksumType;
		};
		prototype.setChecksumType = function(checksumType) {
			this.checksumType = checksumType;
		};
	}, {
	});
	return DataSetFileCreation;
})