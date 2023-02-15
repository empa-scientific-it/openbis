/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/query/fetchoptions/QuerySortOptions" ], function(require, stjs,
		FetchOptions) {
	var QueryFetchOptions = function() {
	};
	stjs.extend(QueryFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.query.fetchoptions.QueryFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var QuerySortOptions = require("as/dto/query/fetchoptions/QuerySortOptions");
				this.sort = new QuerySortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		sort : "QuerySortOptions"
	});
	return QueryFetchOptions;
})