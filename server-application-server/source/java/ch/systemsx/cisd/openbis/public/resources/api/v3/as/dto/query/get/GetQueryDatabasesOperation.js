/**
 * @author pkupczyk
 */
define(['stjs', 'as/dto/common/get/GetObjectsOperation'], function (
  stjs,
  GetObjectsOperation
) {
  var GetQueryDatabasesOperation = function (objectIds, fetchOptions) {
    GetObjectsOperation.call(this, objectIds, fetchOptions)
  }
  stjs.extend(
    GetQueryDatabasesOperation,
    GetObjectsOperation,
    [GetObjectsOperation],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.get.GetQueryDatabasesOperation'
      prototype.getMessage = function () {
        return 'GetQueryDatabasesOperation'
      }
    },
    {}
  )
  return GetQueryDatabasesOperation
})
