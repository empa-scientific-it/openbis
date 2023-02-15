/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var PhysicalDataUpdate = function() {
		this.fileFormatTypeId = new FieldUpdateValue();
		this.archivingRequested = new FieldUpdateValue();
	};
	stjs.extend(PhysicalDataUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.PhysicalDataUpdate';
		constructor.serialVersionUID = 1;

		/*
		 * @Deprecated
		 */
		prototype.fileFormatTypeId = null;
		prototype.archivingRequested = null;

		/*
		 * @Deprecated
		 */
		prototype.getFileFormatTypeId = function() {
			return this.fileFormatTypeId;
		};
		/*
		 * @Deprecated
		 */
		prototype.setFileFormatTypeId = function(fileFormatTypeId) {
			this.fileFormatTypeId.setValue(fileFormatTypeId);
		};
		prototype.isArchivingRequested = function() {
			return this.archivingRequested;
		};
		prototype.setArchivingRequested = function(archivingRequested) {
			this.archivingRequested.setValue(archivingRequested);
		};
	}, {
		fileFormatTypeId : {
			name : "FieldUpdateValue",
			arguments : [ "IFileFormatTypeId" ]
		},
		archivingRequested : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		}
	});
	return PhysicalDataUpdate;
})