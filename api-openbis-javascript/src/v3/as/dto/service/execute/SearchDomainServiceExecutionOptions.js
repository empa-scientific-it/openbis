define([ "stjs", "as/dto/service/execute/AbstractExecutionOptionsWithParameters"], function(stjs, AbstractExecutionOptionsWithParameters) {
	var SearchDomainServiceExecutionOptions = function() {
		AbstractExecutionOptionsWithParameters.call(this);
	};
	stjs.extend(SearchDomainServiceExecutionOptions, AbstractExecutionOptionsWithParameters, [AbstractExecutionOptionsWithParameters], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.service.execute.SearchDomainServiceExecutionOptions';
		constructor.serialVersionUID = 1;
		prototype.preferredSearchDomain = null;
		prototype.searchString = null;
		prototype.withPreferredSearchDomain = function(preferredSearchDomain) {
			this.preferredSearchDomain = preferredSearchDomain;
			return this;
		}
		prototype.getPreferredSearchDomain = function() {
			return this.preferredSearchDomain;
		}
		prototype.withSearchString = function(searchString) {
			this.searchString = searchString;
			return this;
		}
		prototype.getSearchString = function() {
			return this.searchString;
		}
	}, {});
	return SearchDomainServiceExecutionOptions;
})
