/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/entity/AbstractEntityUpdate", "as/dto/common/update/IdListUpdateValue" ],
 function(stjs, AbstractEntityUpdate, IdListUpdateValue) {
	var MaterialUpdate = function() {
	    AbstractEntityUpdate.call(this);
		this.properties = {};
		this.tagIds = new IdListUpdateValue();
	};
	stjs.extend(MaterialUpdate, AbstractEntityUpdate, [AbstractEntityUpdate], function(constructor, prototype) {
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
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagActions = function(actions) {
			this.tagIds.setActions(actions);
		};
	}, {
		materialId : "IMaterialId",
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		}
	});
	return MaterialUpdate;
})