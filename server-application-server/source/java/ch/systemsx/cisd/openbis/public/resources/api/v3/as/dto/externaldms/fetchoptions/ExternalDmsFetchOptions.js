/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/externaldms/fetchoptions/ExternalDmsSortOptions" ], function(require, stjs, FetchOptions) {
	var ExternalDmsFetchOptions = function() {
	};
	stjs.extend(ExternalDmsFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.externaldms.fetchoptions.ExternalDmsFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExternalDmsSortOptions = require("as/dto/externaldms/fetchoptions/ExternalDmsSortOptions");
				this.sort = new ExternalDmsSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "ExternalDmsSortOptions"
	});
	return ExternalDmsFetchOptions;
})