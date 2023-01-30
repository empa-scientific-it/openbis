define([ "require", "stjs", "as/dto/common/fetchoptions/SortOptions" ], function(require, stjs, SortOptions) {
  var RoleAssignmentSortOptions = function() {
    SortOptions.call(this);
  };
  stjs.extend(RoleAssignmentSortOptions, SortOptions, [ SortOptions ], function(constructor, prototype) {
    prototype['@type'] = 'as.dto.roleassignment.fetchoptions.RoleAssignmentSortOptions';
    constructor.serialVersionUID = 1;
  }, {});
  return RoleAssignmentSortOptions;
})