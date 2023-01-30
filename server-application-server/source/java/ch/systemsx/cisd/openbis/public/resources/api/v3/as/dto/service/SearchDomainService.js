define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var SearchDomainService = function() {
	};
	stjs.extend(SearchDomainService, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.SearchDomainService';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.name = null;
		prototype.label = null;
		prototype.possibleSearchOptionsKey = null;
		prototype.possibleSearchOptions = null;

		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getName = function() {
			return this.name;
		};
		prototype.setName = function(name) {
			this.name = name;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getPossibleSearchOptionsKey = function() {
			return this.possibleSearchOptionsKey;
		};
		prototype.setPossibleSearchOptionsKey = function(possibleSearchOptionsKey) {
			this.possibleSearchOptionsKey = possibleSearchOptionsKey;
		};
		prototype.getPossibleSearchOptions = function() {
			return this.possibleSearchOptions;
		};
		prototype.setPossibleSearchOptions = function(possibleSearchOptions) {
			this.possibleSearchOptions = possibleSearchOptions;
		};
		prototype.toString = function() {
			return "SearchDomainService: " + this.permId;
		};
	}, {
		fetchOptions : "SearchDomainServiceFetchOptions"
	});
	return SearchDomainService;
})