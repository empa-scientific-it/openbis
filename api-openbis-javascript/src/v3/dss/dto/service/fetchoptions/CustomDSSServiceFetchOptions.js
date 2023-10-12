define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "dss/dto/service/fetchoptions/CustomDSSServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var CustomDSSServiceFetchOptions = function() {
  };
  stjs.extend(CustomDSSServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'dss.dto.service.fetchoptions.CustomDSSServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var CustomDSSServiceSortOptions = require("dss/dto/service/fetchoptions/CustomDSSServiceSortOptions");
        this.sort = new CustomDSSServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "CustomDSSServiceSortOptions"
  });
  return CustomDSSServiceFetchOptions;
})