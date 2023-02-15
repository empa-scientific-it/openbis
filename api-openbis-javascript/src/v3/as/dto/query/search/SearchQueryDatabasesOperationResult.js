/**
 * @author pkupczyk
 */
define(['stjs', 'as/dto/common/search/SearchObjectsOperationResult'], function (
  stjs,
  SearchObjectsOperationResult
) {
  var SearchQueryDatabasesOperationResult = function (searchResult) {
    SearchObjectsOperationResult.call(this, searchResult)
  }
  stjs.extend(
    SearchQueryDatabasesOperationResult,
    SearchObjectsOperationResult,
    [SearchObjectsOperationResult],
    function (constructor, prototype) {
      prototype['@type'] =
        'as.dto.query.search.SearchQueryDatabasesOperationResult'
      prototype.getMessage = function () {
        return 'SearchQueryDatabasesOperationResult'
      }
    },
    {}
  )
  return SearchQueryDatabasesOperationResult
})
