define(['stjs', 'as/dto/common/delete/DeleteObjectsOperation'], function (
  stjs,
  DeleteObjectsOperation
) {
  var DeletePersonsOperation = function (objectIds, options) {
    DeleteObjectsOperation.call(this, objectIds, options)
  }
  stjs.extend(
    DeletePersonsOperation,
    DeleteObjectsOperation,
    [DeleteObjectsOperation],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.person.delete.DeletePersonsOperation'
      prototype.getMessage = function () {
        return 'DeletePersonsOperation'
      }
    },
    {}
  )
  return DeletePersonsOperation
})
