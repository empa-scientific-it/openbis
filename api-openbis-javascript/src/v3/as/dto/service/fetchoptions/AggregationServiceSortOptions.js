define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var AggregationServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(AggregationServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.AggregationServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return AggregationServiceSortOptions;
})
