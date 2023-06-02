define([ "stjs", "as/dto/common/update/FieldUpdateValue",
            "as/dto/entitytype/update/PropertyAssignmentListUpdateValue",
            "as/dto/common/update/ListUpdateMapValues" ], function(stjs, FieldUpdateValue,
            PropertyAssignmentListUpdateValue, ListUpdateMapValues) {
	var SampleTypeUpdate = function() {
		this.description = new FieldUpdateValue();
		this.generatedCodePrefix = new FieldUpdateValue();
		this.autoGeneratedCode = new FieldUpdateValue();
		this.subcodeUnique = new FieldUpdateValue();
		this.listable = new FieldUpdateValue();
		this.showContainer = new FieldUpdateValue();
		this.showParents = new FieldUpdateValue();
		this.showParentMetadata = new FieldUpdateValue();
		this.validationPluginId = new FieldUpdateValue();
		this.propertyAssignments = new PropertyAssignmentListUpdateValue();
		this.metaData = new ListUpdateMapValues();
	};
	stjs.extend(SampleTypeUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.sample.update.SampleTypeUpdate';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.description = null;
		prototype.generatedCodePrefix = null;
		prototype.autoGeneratedCode = null;
		prototype.subcodeUnique = null;
		prototype.listable = null;
		prototype.showContainer = null;
		prototype.showParents = null;
		prototype.showParentMetadata = null;
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
		prototype.setGeneratedCodePrefix = function(generatedCodePrefix) {
			this.generatedCodePrefix.setValue(generatedCodePrefix);
		};
		prototype.getGeneratedCodePrefix = function() {
			return this.generatedCodePrefix;
		};
		prototype.isAutoGeneratedCode = function() {
			return this.autoGeneratedCode;
		};
		prototype.setAutoGeneratedCode = function(autoGeneratedCode) {
			this.autoGeneratedCode.setValue(autoGeneratedCode);
		};
		prototype.isSubcodeUnique = function() {
			return this.subcodeUnique;
		};
		prototype.setSubcodeUnique = function(subcodeUnique) {
			this.subcodeUnique.setValue(subcodeUnique);
		};
		prototype.isListable = function() {
			return this.listable;
		};
		prototype.setListable = function(listable) {
			this.listable.setValue(listable);
		};
		prototype.isShowContainer = function() {
			return this.showContainer;
		};
		prototype.setShowContainer = function(showContainer) {
			this.showContainer.setValue(showContainer);
		};
		prototype.isShowParents = function() {
			return this.showParents;
		};
		prototype.setShowParents = function(showParents) {
			this.showParents.setValue(showParents);
		};
		prototype.isShowParentMetadata = function() {
			return this.showParentMetadata;
		};
		prototype.setShowParentMetadata = function(showParentMetadata) {
			this.showParentMetadata.setValue(showParentMetadata);
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
		generatedCodePrefix : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		autoGeneratedCode : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
		subcodeUnique : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
		listable : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
		showContainer : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
		showParents : {
			name : "FieldUpdateValue",
			arguments : [ "Boolean" ]
		},
		showParentMetadata : {
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
	return SampleTypeUpdate;
})