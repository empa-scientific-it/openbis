/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var PropertyAssignmentCreation = function() {
	};
	stjs.extend(PropertyAssignmentCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.create.PropertyAssignmentCreation';
		constructor.serialVersionUID = 1;
		prototype.section = null;
		prototype.ordinal = null;
		prototype.propertyTypeId = null;
		prototype.pluginId = null;
		prototype.mandatory = false;
		prototype.initialValueForExistingEntities = null;
		prototype.showInEditView = false;
		prototype.showRawValueInForms = false;
		prototype.unique = false;

		prototype.getSection = function() {
			return this.section;
		};
		prototype.setSection = function(section) {
			this.section = section;
		};
		prototype.getOrdinal = function() {
			return this.ordinal;
		};
		prototype.setOrdinal = function(ordinal) {
			this.ordinal = ordinal;
		};
		prototype.getPropertyTypeId = function() {
			return this.propertyTypeId;
		};
		prototype.setPropertyTypeId = function(propertyTypeId) {
			this.propertyTypeId = propertyTypeId;
		};
		prototype.getPluginId = function() {
			return this.pluginId;
		};
		prototype.setPluginId = function(pluginId) {
			this.pluginId = pluginId;
		};
		prototype.isMandatory = function() {
			return this.mandatory;
		};
		prototype.setMandatory = function(mandatory) {
			this.mandatory = mandatory;
		};
		prototype.getInitialValueForExistingEntities = function() {
			return this.initialValueForExistingEntities;
		};
		prototype.setInitialValueForExistingEntities = function(initialValueForExistingEntities) {
			this.initialValueForExistingEntities = initialValueForExistingEntities;
		};
		prototype.isShowInEditView = function() {
			return this.showInEditView;
		};
		prototype.setShowInEditView = function(showInEditView) {
			this.showInEditView = showInEditView;
		};
		prototype.isShowRawValueInForms = function() {
			return this.showRawValueInForms;
		};
		prototype.setShowRawValueInForms = function(showRawValueInForms) {
			this.showRawValueInForms = showRawValueInForms;
		};
		prototype.isUnique = function() {
            return this.unique;
        };
        prototype.setUnique = function(unique) {
            this.unique = unique;
        };
	}, {
		propertyTypeId : "IPropertyTypeId",
		pluginId : "IPluginId"
	});
	return PropertyAssignmentCreation;
})