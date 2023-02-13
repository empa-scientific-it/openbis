define([ "stjs" ], function(stjs) {
	var FullDataSetCreation = function() {
	};
	stjs.extend(FullDataSetCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.dataset.create.FullDataSetCreation';
		constructor.serialVersionUID = 1;
		prototype.metadataCreation = null;
		prototype.fileMetadata = null;
		
		prototype.getMetadataCreation = function() {
			return this.metadataCreation;
		};
		prototype.setMetadataCreation = function(metadataCreation) {
			this.metadataCreation = metadataCreation;
		};
		prototype.getFileMetadata = function() {
			return this.fileMetadata;
		};
		prototype.setFileMetadata = function(fileMetadata) {
			this.fileMetadata = fileMetadata;
		};
	}, {
		metadataCreation : "DataSetCreation",
		fileMetadata : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return FullDataSetCreation;
})