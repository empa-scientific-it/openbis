define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var AbstractEntityPropertyHolder = function() {
	};
	stjs.extend(AbstractEntityPropertyHolder, null, [], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.common.entity.AbstractEntityPropertyHolder';
        constructor.serialVersionUID = 1;
        prototype.properties = null;

        prototype.getProperty = function(propertyName) {
            var properties = this.getProperties();
            return properties ? properties[propertyName] : null;
        };
        prototype.setProperty = function(propertyName, propertyValue) {
            if (this.properties == null) {
                this.properties = {};
            }
            this.properties[propertyName] = propertyValue;
        };
        prototype.getPropertyAsString = function(propertyName) {
            return this.getProperty(propertyName);
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



        prototype.getMultiValueIntegerProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueIntegerProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueVarcharProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueVarcharProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueMultilineVarcharProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueMultilineVarcharProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueRealProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueRealProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueTimestampProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueTimestampProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueBooleanProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueBooleanProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueControlledVocabularyProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueControlledVocabularyProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueSampleProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueSampleProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueHyperlinkProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueHyperlinkProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueXmlProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueXmlProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueIntegerArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueIntegerArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueRealArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueRealArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueStringArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueStringArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueTimestampArrayProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueTimestampArrayProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };
        prototype.getMultiValueJsonProperty = function(propertyName) {
            return this.getProperty(propertyName);
        };
        prototype.setMultiValueJsonProperty = function(propertyName, propertyValue) {
            this.setProperty(propertyName, propertyValue);
        };



    }, {
        properties : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        }
    });
    return AbstractEntityPropertyHolder;
})