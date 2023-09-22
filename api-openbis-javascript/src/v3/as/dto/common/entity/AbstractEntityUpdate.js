define([ "stjs", "util/Exceptions", "as/dto/common/entity/AbstractEntityPropertyHolder" ], function(stjs, exceptions, AbstractEntityPropertyHolder) {
	var AbstractEntityUpdate = function() {
        AbstractEntityPropertyHolder.call(this);
	};
	stjs.extend(AbstractEntityUpdate, null, [], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.common.entity.AbstractEntityUpdate';
        constructor.serialVersionUID = 1;

        prototype.getProperties = function() {
          return properties;
        };

        prototype.setProperties = function(properties) {
            this.properties = properties;
        };

    }, {});
    return AbstractEntityUpdate;
})