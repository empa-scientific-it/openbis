/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var TagCreation = function() {
	};
	stjs.extend(TagCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.create.TagCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;
		prototype.experimentIds = null;
		prototype.sampleIds = null;
		prototype.dataSetIds = null;
		prototype.materialIds = null;

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
		prototype.getExperimentIds = function() {
			return this.experimentIds;
		};
		prototype.setExperimentIds = function(experimentIds) {
			this.experimentIds = experimentIds;
		};
		prototype.getSampleIds = function() {
			return this.sampleIds;
		};
		prototype.setSampleIds = function(sampleIds) {
			this.sampleIds = sampleIds;
		};
		prototype.getDataSetIds = function() {
			return this.dataSetIds;
		};
		prototype.setDataSetIds = function(dataSetIds) {
			this.dataSetIds = dataSetIds;
		};
		prototype.getMaterialIds = function() {
			return this.materialIds;
		};
		prototype.setMaterialIds = function(materialIds) {
			this.materialIds = materialIds;
		};
	}, {
		experimentIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		sampleIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		dataSetIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		materialIds : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return TagCreation;
})