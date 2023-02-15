define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/service/fetchoptions/ProcessingServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var ProcessingServiceFetchOptions = function() {
  };
  stjs.extend(ProcessingServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.ProcessingServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var ProcessingServiceSortOptions = require("as/dto/service/fetchoptions/ProcessingServiceSortOptions");
        this.sort = new ProcessingServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "ProcessingServiceSortOptions"
  });
  return ProcessingServiceFetchOptions;
})
