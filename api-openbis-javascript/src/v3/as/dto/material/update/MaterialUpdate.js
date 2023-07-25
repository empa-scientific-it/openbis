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
		prototype.getIntegerProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setIntegerProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getVarcharProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setVarcharProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultilineVarcharProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultilineVarcharProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getRealProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setRealProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getTimestampProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setTimestampProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getBooleanProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setBooleanProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getControlledVocabularyProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setControlledVocabularyProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getSampleProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setSampleProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getHyperlinkProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setHyperlinkProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getXmlProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setXmlProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getIntegerArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setIntegerArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getRealArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setRealArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getStringArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setStringArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getTimestampArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setTimestampArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getJsonProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setJsonProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
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
			arguments : [ "String", "Serializable" ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		}
	});
	return MaterialUpdate;
})