define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/service/fetchoptions/SearchDomainServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var SearchDomainServiceFetchOptions = function() {
  };
  stjs.extend(SearchDomainServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.SearchDomainServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var SearchDomainServiceSortOptions = require("as/dto/service/fetchoptions/SearchDomainServiceSortOptions");
        this.sort = new SearchDomainServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "SearchDomainServiceSortOptions"
  });
  return SearchDomainServiceFetchOptions;
})
