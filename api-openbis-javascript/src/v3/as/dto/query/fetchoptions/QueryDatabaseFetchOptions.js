/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([
  'require',
  'stjs',
  'as/dto/common/fetchoptions/FetchOptions',
  'as/dto/space/fetchoptions/SpaceFetchOptions',
  'as/dto/query/fetchoptions/QueryDatabaseSortOptions'
], function (require, stjs, FetchOptions) {
  var QueryDatabaseFetchOptions = function () {}
  stjs.extend(
    QueryDatabaseFetchOptions,
    FetchOptions,
    [FetchOptions],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.fetchoptions.QueryDatabaseFetchOptions'
      constructor.serialVersionUID = 1
      prototype.space = null
      prototype.sort = null
      prototype.withSpace = function () {
        if (this.space == null) {
          var SpaceFetchOptions = require('as/dto/space/fetchoptions/SpaceFetchOptions')
          this.space = new SpaceFetchOptions()
        }
        return this.space
      }
      prototype.withSpaceUsing = function (fetchOptions) {
        return (this.space = fetchOptions)
      }
      prototype.hasSpace = function () {
        return this.space != null
      }
      prototype.sortBy = function () {
        if (this.sort == null) {
          var QueryDatabaseSortOptions = require('as/dto/query/fetchoptions/QueryDatabaseSortOptions')
          this.sort = new QueryDatabaseSortOptions()
        }
        return this.sort
      }
      prototype.getSortBy = function () {
        return this.sort
      }
    },
    {
      space: 'SpaceFetchOptions',
      sort: 'QueryDatabaseSortOptions'
    }
  )
  return QueryDatabaseFetchOptions
})
