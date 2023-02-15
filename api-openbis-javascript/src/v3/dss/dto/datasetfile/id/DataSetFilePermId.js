/**
 * Data set file perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "util/Exceptions", "dss/dto/datasetfile/id/IDataSetFileId" ], function(stjs, exceptions, IDataSetFileId) {
	/**
	 * Data set root file perm id
	 */
	var DataSetFilePermId = function(dataSetId, filePath) {
		this.dataSetId = dataSetId ? dataSetId : null;
		this.filePath = filePath ? filePath : null;
	};

	stjs.extend(DataSetFilePermId, null, [ IDataSetFileId ], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.datasetfile.id.DataSetFilePermId';
		constructor.serialVersionUID = 1;
		prototype.dataSetId = null;
		prototype.filePath = null;

		prototype.getDataSetId = function() {
			return this.dataSetId;
		};
		prototype.setDataSetId = function(dataSetId) {
			this.dataSetId = dataSetId;
		};
		prototype.getFilePath = function() {
			return this.filePath;
		};
		prototype.setFilePath = function(filePath) {
			this.filePath = filePath;
		};
		prototype.toString = function() {
			return this.getDataSetId() + "#" + this.getFilePath();
		};
		prototype.hashCode = function() {
			throw new exceptions.RuntimeException("Unsupported method.");
		};
		prototype.equals = function() {
			throw new exceptions.RuntimeException("Unsupported method.");
		};
	}, {
		dataSetId : "IDataSetId"
	});
	return DataSetFilePermId;
})