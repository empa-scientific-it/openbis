/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var ExperimentTypeCreation = function() {
	};
	stjs.extend(ExperimentTypeCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.create.ExperimentTypeCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;
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
	return ExperimentTypeCreation;
})