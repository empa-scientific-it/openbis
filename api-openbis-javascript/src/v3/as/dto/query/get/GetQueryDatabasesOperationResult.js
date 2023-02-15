/**
 * @author pkupczyk
 */
define(['stjs', 'as/dto/common/get/GetObjectsOperationResult'], function (
  stjs,
  GetObjectsOperationResult
) {
  var GetQueryDatabasesOperationResult = function (objectMap) {
    GetObjectsOperationResult.call(this, objectMap)
  }
  stjs.extend(
    GetQueryDatabasesOperationResult,
    GetObjectsOperationResult,
    [GetObjectsOperationResult],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.query.get.GetQueryDatabasesOperationResult'
      prototype.getMessage = function () {
        return 'GetQueryDatabasesOperationResult'
      }
    },
    {}
  )
  return GetQueryDatabasesOperationResult
})
