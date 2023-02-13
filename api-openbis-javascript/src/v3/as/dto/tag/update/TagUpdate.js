/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue) {
	var TagUpdate = function() {
		this.description = new FieldUpdateValue();
		this.experimentIds = new IdListUpdateValue();
		this.sampleIds = new IdListUpdateValue();
		this.dataSetIds = new IdListUpdateValue();
		this.materialIds = new IdListUpdateValue();
	};
	stjs.extend(TagUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.update.TagUpdate';
		constructor.serialVersionUID = 1;
		prototype.tagId = null;
		prototype.description = null;
		prototype.experimentIds = null;
		prototype.sampleIds = null;
		prototype.dataSetIds = null;
		prototype.materialIds = null;

		prototype.getObjectId = function() {
			return this.getTagId();
		};
		prototype.getTagId = function() {
			return this.tagId;
		};
		prototype.setTagId = function(tagId) {
			this.tagId = tagId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getExperimentIds = function() {
			return this.experimentIds;
		};
		prototype.setExperimentActions = function(actions) {
			this.experimentIds.setActions(actions);
		};
		prototype.getSampleIds = function() {
			return this.sampleIds;
		};
		prototype.setSampleActions = function(actions) {
			this.sampleIds.setActions(actions);
		};
		prototype.getDataSetIds = function() {
			return this.dataSetIds;
		};
		prototype.setDataSetActions = function(actions) {
			this.dataSetIds.setActions(actions);
		};
		prototype.getMaterialIds = function() {
			return this.materialIds;
		};
		prototype.setMaterialActions = function(actions) {
			this.materialIds.setActions(actions);
		};
	}, {
		tagId : "ITagId",
		description : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		experimentIds : {
			name : "IdListUpdateValue",
			arguments : [ "IExperimentId" ]
		},
		sampleIds : {
			name : "IdListUpdateValue",
			arguments : [ "ISampleId" ]
		},
		dataSetIds : {
			name : "IdListUpdateValue",
			arguments : [ "IDataSetId" ]
		},
		materialIds : {
			name : "IdListUpdateValue",
			arguments : [ "IMaterialId" ]
		}
	});
	return TagUpdate;
})