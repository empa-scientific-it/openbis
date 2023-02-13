define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/service/fetchoptions/AggregationServiceSortOptions" ], function(require, stjs, FetchOptions) {
  var AggregationServiceFetchOptions = function() {
  };
  stjs.extend(AggregationServiceFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.AggregationServiceFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var AggregationServiceSortOptions = require("as/dto/service/fetchoptions/AggregationServiceSortOptions");
        this.sort = new AggregationServiceSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "AggregationServiceSortOptions"
  });
  return AggregationServiceFetchOptions;
})
