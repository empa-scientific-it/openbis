define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/service/fetchoptions/ReportingServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var ReportingServiceFetchOptions = function() {
  };
  stjs.extend(ReportingServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.ReportingServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var ReportingServiceSortOptions = require("as/dto/service/fetchoptions/ReportingServiceSortOptions");
        this.sort = new ReportingServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "ReportingServiceSortOptions"
  });
  return ReportingServiceFetchOptions;
})
