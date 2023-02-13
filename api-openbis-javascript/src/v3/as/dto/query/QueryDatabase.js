/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(['stjs', 'util/Exceptions'], function (stjs, exceptions) {
  var QueryDatabase = function () {}
  stjs.extend(
    QueryDatabase,
    null,
    [],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.QueryDatabase'
      constructor.serialVersionUID = 1
      prototype.fetchOptions = null
      prototype.permId = null
      prototype.name = null
      prototype.label = null
      prototype.space = null
      prototype.creatorMinimalRole = null
      prototype.creatorMinimalRoleLevel = null

      prototype.getFetchOptions = function () {
        return this.fetchOptions
      }
      prototype.setFetchOptions = function (fetchOptions) {
        this.fetchOptions = fetchOptions
      }
      prototype.getPermId = function () {
        return this.permId
      }
      prototype.setPermId = function (permId) {
        this.permId = permId
      }
      prototype.getName = function () {
        return this.name
      }
      prototype.setName = function (name) {
        this.name = name
      }
      prototype.getLabel = function () {
        return this.label
      }
      prototype.setLabel = function (label) {
        this.label = label
      }
      prototype.getSpace = function () {
        if (this.getFetchOptions() && this.getFetchOptions().hasSpace()) {
          return this.space
        } else {
          throw new exceptions.NotFetchedException(
            'Space has not been fetched.'
          )
        }
      }
      prototype.setSpace = function (space) {
        this.space = space
      }
      prototype.getCreatorMinimalRole = function () {
        return this.creatorMinimalRole
      }
      prototype.setCreatorMinimalRole = function (creatorMinimalRole) {
        this.creatorMinimalRole = creatorMinimalRole
      }
      prototype.getCreatorMinimalRoleLevel = function () {
        return this.creatorMinimalRoleLevel
      }
      prototype.setCreatorMinimalRoleLevel = function (
        creatorMinimalRoleLevel
      ) {
        this.creatorMinimalRoleLevel = creatorMinimalRoleLevel
      }
    },
    {
      fetchOptions: 'QueryDatabaseFetchOptions',
      permId: 'QueryDatabaseName',
      space: 'Space',
      creatorMinimalRole: 'Role',
      creatorMinimalRoleLevel: 'RoleLevel'
    }
  )
  return QueryDatabase
})
