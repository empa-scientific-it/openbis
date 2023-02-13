define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var CustomASServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(CustomASServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.service.fetchoptions.CustomASServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return CustomASServiceSortOptions;
})