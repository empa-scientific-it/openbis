define(['require', 'stjs', 'as/dto/common/fetchoptions/SortOptions'], function (
  require,
  stjs,
  SortOptions
) {
  var QueryDatabaseSortOptions = function () {
    SortOptions.call(this)
  }

  var fields = {
    NAME: 'NAME'
  }

  stjs.extend(
    QueryDatabaseSortOptions,
    SortOptions,
    [SortOptions],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.fetchoptions.QueryDatabaseSortOptions'
      constructor.serialVersionUID = 1

      prototype.name = function () {
        return this.getOrCreateSorting(fields.NAME)
      }
      prototype.getName = function () {
        return this.getSorting(fields.NAME)
      }
    },
    {}
  )
  return QueryDatabaseSortOptions
})
