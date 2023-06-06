define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/entitytype/update/PropertyAssignmentListUpdateValue",
 "as/dto/common/update/ListUpdateMapValues"], function(stjs, FieldUpdateValue,
		PropertyAssignmentListUpdateValue, ListUpdateMapValues) {
	var DataSetTypeUpdate = function() {
		this.description = new FieldUpdateValue();
		this.mainDataSetPattern = new FieldUpdateValue();
		this.mainDataSetPath = new FieldUpdateValue();
		this.disallowDeletion = new FieldUpdateValue();
		this.validationPluginId = new FieldUpdateValue();
		this.propertyAssignments = new PropertyAssignmentListUpdateValue();
		this.metaData = new ListUpdateMapValues();
	};
	stjs.extend(DataSetTypeUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.DataSetTypeUpdate';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.description = null;
		prototype.mainDataSetPattern = null;
		prototype.mainDataSetPath = null;
		prototype.disallowDeletion = null;
		prototype.validationPluginId = null;
		prototype.propertyAssignments = null;
		prototype.metaData = null;

		prototype.getObjectId = function() {
			return this.getTypeId();
		};
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setMainDataSetPattern = function(mainDataSetPattern) {
			this.mainDataSetPattern.setValue(mainDataSetPattern);
		};
		prototype.getMainDataSetPattern = function() {
			return this.mainDataSetPattern;
		};
		prototype.setMainDataSetPath = function(mainDataSetPath) {
			this.mainDataSetPath.setValue(mainDataSetPath);
		};
		prototype.getMainDataSetPath = function() {
			return this.mainDataSetPath;
		};
		prototype.setDisallowDeletion = function(disallowDeletion) {
			this.disallowDeletion.setValue(disallowDeletion);
		};
		prototype.isDisallowDeletion = function() {
			return this.disallowDeletion;
		};
		prototype.setValidationPluginId = function(validationPluginId) {
			this.validationPluginId.setValue(validationPluginId);
		};
		prototype.getValidationPluginId = function() {
			return this.validationPluginId;
		};
		prototype.getPropertyAssignments = function() {
			return this.propertyAssignments;
		};
		prototype.setPropertyAssignmentActions = function(actions) {
			this.propertyAssignments.setActions(actions);
		};
		prototype.getMetaData = function() {
            return this.metaData;
        };
        prototype.setMetaDataActions = function(actions) {
            this.metaData.setActions(actions);
        };
	}, {
		typeId : "IEntityTypeId",
		description : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		mainDataSetPattern : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		mainDataSetPath : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		disallowDeletion : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
		validationPluginId : {
			name : "FieldUpdateValue",
			arguments : [ "IPluginId" ]
		},
		propertyAssignments : "PropertyAssignmentListUpdateValue",
		metaData : "ListUpdateMapValues"
	});
	return DataSetTypeUpdate;
})