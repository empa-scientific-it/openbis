/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var DataSetTypeCreation = function() {
	};
	stjs.extend(DataSetTypeCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.create.DataSetTypeCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;
		prototype.mainDataSetPattern = null;
		prototype.mainDataSetPath = null;
		prototype.disallowDeletion = false;
		prototype.validationPluginId = null;
		prototype.propertyAssignments = null;

		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getMainDataSetPattern = function() {
			return this.mainDataSetPattern;
		};
		prototype.setMainDataSetPattern = function(mainDataSetPattern) {
			this.mainDataSetPattern = mainDataSetPattern;
		};
		prototype.getMainDataSetPath = function() {
			return this.mainDataSetPath;
		};
		prototype.setMainDataSetPath = function(mainDataSetPath) {
			this.mainDataSetPath = mainDataSetPath;
		};
		prototype.isDisallowDeletion = function() {
			return this.disallowDeletion;
		};
		prototype.setDisallowDeletion = function(disallowDeletion) {
			this.disallowDeletion = disallowDeletion;
		};
		prototype.getValidationPluginId = function() {
			return this.validationPluginId;
		};
		prototype.setValidationPluginId = function(validationPluginId) {
			this.validationPluginId = validationPluginId;
		};
		prototype.getPropertyAssignments = function() {
			return this.propertyAssignments;
		};
		prototype.setPropertyAssignments = function(propertyAssignments) {
			this.propertyAssignments = propertyAssignments;
		};

	}, {
		validationPluginId : "IPluginId",
		propertyAssignments : {
			name : "List",
			arguments : [ "PropertyAssignmentCreation" ]
		}
	});
	return DataSetTypeCreation;
})