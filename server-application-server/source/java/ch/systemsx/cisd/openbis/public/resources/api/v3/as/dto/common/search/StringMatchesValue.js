define([ "stjs", "as/dto/common/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
    var StringMatchesValue = function(value) {
        AbstractStringValue.call(this, value);
    };
    stjs.extend(StringMatchesValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.common.search.StringMatchesValue';
        constructor.serialVersionUID = 1;
        prototype.toString = function() {
            return "matches '" + this.getValue() + "'";
        };
    }, {});
    return StringMatchesValue;
})