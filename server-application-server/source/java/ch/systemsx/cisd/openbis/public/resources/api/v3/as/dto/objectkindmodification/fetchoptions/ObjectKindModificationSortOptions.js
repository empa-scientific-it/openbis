define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var ObjectKindModificationSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(ObjectKindModificationSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.objectkindmodification.fetchoptions.ObjectKindModificationSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return ObjectKindModificationSortOptions;
})