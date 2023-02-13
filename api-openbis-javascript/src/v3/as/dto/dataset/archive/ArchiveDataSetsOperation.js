/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/operation/IOperation" ], function(stjs, IOperation) {
	var ArchiveDataSetsOperation = function(dataSetIds, options) {
		this.dataSetIds = dataSetIds;
		this.options = options;
	};
	stjs.extend(ArchiveDataSetsOperation, null, [ IOperation ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.archive.ArchiveDataSetsOperation';
		prototype.dataSetIds = null;
		prototype.options = null;
		prototype.getDataSetIds = function() {
			return this.dataSetIds;
		};
		prototype.getOptions = function() {
			return this.options;
		};
		prototype.getMessage = function() {
			return "ArchiveDataSetsOperation";
		};
	}, {
		dataSetIds : {
			name : "List",
			arguments : [ "IDataSetId" ]
		},
		options : "DataSetArchiveOptions"
	});
	return ArchiveDataSetsOperation;
})