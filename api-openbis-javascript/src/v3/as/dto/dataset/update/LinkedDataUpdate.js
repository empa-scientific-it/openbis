/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/dataset/update/ContentCopyListUpdateValue" ], function(stjs, FieldUpdateValue, ContentCopyListUpdateValue) {
	var LinkedDataUpdate = function() {
		this.externalCode = new FieldUpdateValue();
		this.externalDmsId = new FieldUpdateValue();
		this.contentCopies = new ContentCopyListUpdateValue();
	};
	stjs.extend(LinkedDataUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.LinkedDataUpdate';
		constructor.serialVersionUID = 1;

		prototype.externalCode = null;
		prototype.externalDmsId = null;
		prototype.contentCopies = null;

		prototype.getExternalCode = function() {
			return this.externalCode;
		};
		prototype.setExternalCode = function(externalCode) {
			this.externalCode.setValue(externalCode);
		};
		prototype.getExternalDmsId = function() {
			return this.externalDmsId;
		};
		prototype.setExternalDmsId = function(externalDmsId) {
			this.externalDmsId.setValue(externalDmsId);
		};
		prototype.getContentCopies = function() {
			return this.externalDmsId;
		};
		prototype.setContentCopyActions = function(actions) {
			this.contentCopies.setActions(actions);
		};
	}, {
		externalCode : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		externalDmsId : {
			name : "FieldUpdateValue",
			arguments : [ "IExternalDmsId" ]
		},
		contentCopies : "ContentCopyListUpdateValue"
	});
	return LinkedDataUpdate;
})