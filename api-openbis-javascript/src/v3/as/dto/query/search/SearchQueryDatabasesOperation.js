/**
 * @author pkupczyk
 */
define(['stjs', 'as/dto/common/search/SearchObjectsOperation'], function (
  stjs,
  SearchObjectsOperation
) {
  var SearchQueryDatabasesOperation = function (criteria, fetchOptions) {
    SearchObjectsOperation.call(this, criteria, fetchOptions)
  }
  stjs.extend(
    SearchQueryDatabasesOperation,
    SearchObjectsOperation,
    [SearchObjectsOperation],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.search.SearchQueryDatabasesOperation'
      prototype.getMessage = function () {
        return 'SearchQueryDatabasesOperation'
      }
    },
    {}
  )
  return SearchQueryDatabasesOperation
})
