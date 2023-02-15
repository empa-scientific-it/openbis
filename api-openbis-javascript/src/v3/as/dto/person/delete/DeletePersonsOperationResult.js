define([
  'stjs',
  'as/dto/common/delete/DeleteObjectsWithoutTrashOperationResult'
], function (stjs, DeleteObjectsWithoutTrashOperationResult) {
  var DeletePersonsOperationResult = function () {
    DeleteObjectsWithoutTrashOperationResult.call(this)
  }
  stjs.extend(
    DeletePersonsOperationResult,
    DeleteObjectsWithoutTrashOperationResult,
    [DeleteObjectsWithoutTrashOperationResult],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.person.delete.DeletePersonsOperationResult'
      prototype.getMessage = function () {
        return 'DeletePersonsOperationResult'
      }
    },
    {}
  )
  return DeletePersonsOperationResult
})
