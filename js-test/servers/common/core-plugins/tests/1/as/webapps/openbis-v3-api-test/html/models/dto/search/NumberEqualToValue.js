/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs, AbstractNumberValue) {
    var NumberEqualToValue = function(number) {
        AbstractNumberValue.call(this, number);
    };
    stjs.extend(NumberEqualToValue, AbstractNumberValue, [AbstractNumberValue], function(constructor, prototype) {
        prototype['@type'] = 'NumberEqualToValue';
        constructor.serialVersionUID = 1;
        prototype.toString = function() {
            return "equal to '" + this.getValue() + "'";
        };
    }, {});
    return NumberEqualToValue;
})