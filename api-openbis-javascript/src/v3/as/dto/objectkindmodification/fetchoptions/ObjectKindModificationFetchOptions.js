define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/objectkindmodification/fetchoptions/ObjectKindModificationSortOptions" ], function(require, stjs, FetchOptions) {
  var ObjectKindModificationFetchOptions = function() {
  };
  stjs.extend(ObjectKindModificationFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.objectkindmodification.fetchoptions.ObjectKindModificationFetchOptions';
    constructor.serialVersionUID = 1;
    prototype.sort = null;
    prototype.sortBy = function() {
      if (this.sort == null) {
        var ObjectKindModificationSortOptions = require("as/dto/objectkindmodification/fetchoptions/ObjectKindModificationSortOptions");
        this.sort = new ObjectKindModificationSortOptions();
      }
      return this.sort;
    };
    prototype.getSortBy = function() {
      return this.sort;
    };
  }, {
    sort : "ObjectKindModificationSortOptions"
  });
  return ObjectKindModificationFetchOptions;
})