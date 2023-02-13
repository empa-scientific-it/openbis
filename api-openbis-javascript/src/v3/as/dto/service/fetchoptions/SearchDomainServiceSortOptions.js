define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var SearchDomainServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(SearchDomainServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.SearchDomainServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return SearchDomainServiceSortOptions;
})
