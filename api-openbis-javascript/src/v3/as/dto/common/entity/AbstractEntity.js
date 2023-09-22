define([ "stjs", "util/Exceptions", "as/dto/common/entity/AbstractEntityPropertyHolder" ], function(stjs, exceptions, AbstractEntityPropertyHolder) {
	var AbstractEntity = function() {
	    AbstractEntityPropertyHolder.call(this);
	};
	stjs.extend(AbstractEntity, AbstractEntityPropertyHolder, [AbstractEntityPropertyHolder], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.common.entity.AbstractEntity';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;

        prototype.getFetchOptions = function() {
            return this.fetchOptions;
        };
        prototype.setFetchOptions = function(fetchOptions) {
            this.fetchOptions = fetchOptions;
        };

        prototype.getProperties = function() {
            if (this.getFetchOptions() && this.getFetchOptions().hasProperties()) {
                return this.properties;
            } else {
                throw new exceptions.NotFetchedException("Properties has not been fetched.");
            }
        };

    }, {
        fetchOptions : "AbstractEntityFetchOptions"
    });
    return AbstractEntity;
})