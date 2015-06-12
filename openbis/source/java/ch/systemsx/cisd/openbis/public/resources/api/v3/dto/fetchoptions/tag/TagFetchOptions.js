/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/person/PersonFetchOptions" ], function(require, stjs) {
	var TagFetchOptions = function() {
	};
	stjs.extend(TagFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.tag.TagFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.owner = null;
		prototype.withOwner = function() {
			if (this.owner == null) {
				var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
				this.owner = new PersonFetchOptions();
			}
			return this.owner;
		};
		prototype.withOwnerUsing = function(fetchOptions) {
			return this.owner = fetchOptions;
		};
		prototype.hasOwner = function() {
			return this.owner != null;
		};
	}, {
		owner : "PersonFetchOptions"
	});
	return TagFetchOptions;
})