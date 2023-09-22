/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/entity/AbstractEntityCreation" ], function(stjs, AbstractEntityCreation) {
	var MaterialCreation = function() {
	    AbstractEntityCreation.call(this);
		this.properties = {};
	};
	stjs.extend(MaterialCreation, AbstractEntityCreation, [AbstractEntityCreation], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.create.MaterialCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.typeId = null;
		prototype.description = null;
		prototype.creationId = null;
		prototype.tagIds = null;

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
	}, {
		typeId : "IEntityTypeId",
		creationId : "CreationId",
		tagIds : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return MaterialCreation;
})