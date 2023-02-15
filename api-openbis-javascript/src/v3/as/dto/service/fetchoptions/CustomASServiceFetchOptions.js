define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/service/fetchoptions/CustomASServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var CustomASServiceFetchOptions = function() {
  };
  stjs.extend(CustomASServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.CustomASServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var CustomASServiceSortOptions = require("as/dto/service/fetchoptions/CustomASServiceSortOptions");
        this.sort = new CustomASServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "CustomASServiceSortOptions"
  });
  return CustomASServiceFetchOptions;
})