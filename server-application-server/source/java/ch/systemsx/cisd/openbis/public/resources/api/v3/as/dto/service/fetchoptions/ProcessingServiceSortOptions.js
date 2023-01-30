define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var ProcessingServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(ProcessingServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.ProcessingServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return ProcessingServiceSortOptions;
})
