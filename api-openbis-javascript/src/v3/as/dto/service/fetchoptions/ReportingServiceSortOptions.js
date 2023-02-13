define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var ReportingServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(ReportingServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.ReportingServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return ReportingServiceSortOptions;
})
