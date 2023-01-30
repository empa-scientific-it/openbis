define([ "require", "stjs", "as/dto/common/search/AbstractSearchCriteria", "as/dto/common/search/StringMatchesValue" ],
function(require, stjs, AbstractSearchCriteria) {
    var TextAttributeSearchCriteria = function(fieldName, fieldType) {
        AbstractSearchCriteria.call(this, fieldName, fieldType);
    };
    stjs.extend(TextAttributeSearchCriteria, AbstractSearchCriteria, [ AbstractSearchCriteria ],
        function(constructor, prototype) {
            prototype['@type'] = 'as.dto.common.search.TextAttributeSearchCriteria';
            constructor.serialVersionUID = 1;
            prototype.fieldValue;

            prototype.thatMatches = function(text) {
                var StringMatchesValue = require("as/dto/common/search/StringMatchesValue");
                this.setFieldValue(new StringMatchesValue(text));
            };
            prototype.getFieldValue = function() {
                return this.fieldValue;
            };
            prototype.toString = function() {
                return "with any text attribute '" +
                    (this.getFieldValue() == null ? '' : this.getFieldValue().toString()) + "'";
            };
        }, {});
    return TextAttributeSearchCriteria;
})
