/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions", "as/dto/common/entity/AbstractEntity" ], function(stjs, exceptions, AbstractEntity) {
	var Material = function() {
	    AbstractEntity.call(this);
	};
	stjs.extend(Material, AbstractEntity, [AbstractEntity], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.Material';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
		prototype.code = null;
		prototype.type = null;
		prototype.history = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.modificationDate = null;
		prototype.materialProperties = null;
		prototype.tags = null;
		prototype.getFetchOptions = function() {
			return AbstractEntity.prototype.getFetchOptions.call(this);
		};
		prototype.setFetchOptions = function(fetchOptions) {
			AbstractEntity.prototype.setFetchOptions.call(this, fetchOptions);
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getType = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Material type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getHistory = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasHistory()) {
				return this.history;
			} else {
				throw new exceptions.NotFetchedException("History has not been fetched.");
			}
		};
		prototype.setHistory = function(history) {
			this.history = history;
		};
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};

		prototype.getMaterialProperty = function(propertyName) {
			var properties = this.getMaterialProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.setMaterialProperty = function(propertyName, propertyValue) {
			if (this.materialProperties == null) {
				this.materialProperties = {};
			}
			this.materialProperties[propertyName] = propertyValue;
		};
		prototype.getMaterialProperties = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasMaterialProperties()) {
				return this.materialProperties;
			} else {
				throw new exceptions.NotFetchedException("Material properties has not been fetched.");
			}
		};
		prototype.setMaterialProperties = function(materialProperties) {
			this.materialProperties = materialProperties;
		};
		prototype.getTags = function() {
			if (this.getFetchOptions() && this.getFetchOptions().hasTags()) {
				return this.tags;
			} else {
				throw new exceptions.NotFetchedException("Tags has not been fetched.");
			}
		};
		prototype.setTags = function(tags) {
			this.tags = tags;
		};
	}, {
		fetchOptions : "MaterialFetchOptions",
		permId : "MaterialPermId",
		type : "MaterialType",
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		registrationDate : "Date",
		registrator : "Person",
		modificationDate : "Date",
		materialProperties : {
			name : "Map",
			arguments : [ "String", "Material" ]
		},
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		}
	});
	return Material;
})