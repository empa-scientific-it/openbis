/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/IdListUpdateValue" ], function(stjs, IdListUpdateValue) {
	var MaterialUpdate = function() {
		this.properties = {};
		this.tagIds = new IdListUpdateValue();
	};
	stjs.extend(MaterialUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.update.MaterialUpdate';
		constructor.serialVersionUID = 1;

		prototype.materialId = null;
		prototype.properties = null;
		prototype.tagIds = null;

		prototype.getObjectId = function() {
			return this.getMaterialId();
		};
		prototype.getMaterialId = function() {
			return this.materialId;
		};
		prototype.setMaterialId = function(materialId) {
			this.materialId = materialId;
		};
		prototype.getProperty = function(propertyName) {
			return this.properties[propertyName];
		};
		prototype.setProperty = function(propertyName, propertyValue) {
			this.properties[propertyName] = propertyValue;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagActions = function(actions) {
			this.tagIds.setActions(actions);
		};
	}, {
		materialId : "IMaterialId",
		properties : {
			name : "Map",
			arguments : [ "String", "String" ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		}
	});
	return MaterialUpdate;
})