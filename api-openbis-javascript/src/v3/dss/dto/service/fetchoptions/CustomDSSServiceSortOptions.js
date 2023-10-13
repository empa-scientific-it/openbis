define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var CustomDSSServiceSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(CustomDSSServiceSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'dss.dto.service.fetchoptions.CustomDSSServiceSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return CustomDSSServiceSortOptions;
})