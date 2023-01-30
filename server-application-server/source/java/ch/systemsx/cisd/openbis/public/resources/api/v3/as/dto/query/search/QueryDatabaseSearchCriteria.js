/**
 * @author pkupczyk
 */
define([
  'require',
  'stjs',
  'as/dto/common/search/AbstractObjectSearchCriteria'
], function (require, stjs, AbstractObjectSearchCriteria) {
  var QueryDatabaseSearchCriteria = function () {
    AbstractObjectSearchCriteria.call(this)
  }
  stjs.extend(
    QueryDatabaseSearchCriteria,
    AbstractObjectSearchCriteria,
    [AbstractObjectSearchCriteria],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.search.QueryDatabaseSearchCriteria'
      constructor.serialVersionUID = 1
    },
    {
      criteria: {
        name: 'Collection',
        arguments: ['ISearchCriteria']
      }
    }
  )
  return QueryDatabaseSearchCriteria
})
