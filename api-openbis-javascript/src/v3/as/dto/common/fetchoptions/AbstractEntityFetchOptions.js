define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", 'as/dto/property/fetchoptions/PropertyFetchOptions' ], function(stjs, FetchOptions) {
	var AbstractEntityFetchOptions = function() {
		FetchOptions.call(this);
	};
	stjs.extend(AbstractEntityFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
        prototype['@type'] = 'as.dto.common.fetchoptions.AbstractEntityFetchOptions';
        constructor.serialVersionUID = 1;
        prototype.properties = null;
        prototype.withProperties = function() {
            if (this.properties == null) {
                var PropertyFetchOptions = require("as/dto/property/fetchoptions/PropertyFetchOptions");
                this.properties = new PropertyFetchOptions();
            }
            return this.properties;
        };
        prototype.withPropertiesUsing = function(fetchOptions) {
            return this.properties = fetchOptions;
        };
        prototype.hasProperties = function() {
            return this.properties != null;
        };
    }, {
        properties : "PropertyFetchOptions",
    });
    return AbstractEntityFetchOptions;
})