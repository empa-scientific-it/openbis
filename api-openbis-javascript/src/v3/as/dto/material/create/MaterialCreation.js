/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var MaterialCreation = function() {
		this.properties = {};
	};
	stjs.extend(MaterialCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.create.MaterialCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.typeId = null;
		prototype.description = null;
		prototype.creationId = null;
		prototype.tagIds = null;
		prototype.properties = null;

		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getCreationId = function() {
			return this.creationId;
		};
		prototype.setCreationId = function(creationId) {
			this.creationId = creationId;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagIds = function(tagIds) {
			this.tagIds = tagIds;
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
	}, {
		typeId : "IEntityTypeId",
		creationId : "CreationId",
		tagIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		properties : {
			name : "Map",
			arguments : [ null, null ]
		}
	});
	return MaterialCreation;
})