define(['stjs', 'as/dto/deletion/AbstractObjectDeletionOptions'], function (
  stjs,
  AbstractObjectDeletionOptions
) {
  var PersonDeletionOptions = function () {
    AbstractObjectDeletionOptions.call(this)
  }
  stjs.extend(
    PersonDeletionOptions,
    AbstractObjectDeletionOptions,
    [AbstractObjectDeletionOptions],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.person.delete.PersonDeletionOptions'
      constructor.serialVersionUID = 1
    },
    {}
  )
  return PersonDeletionOptions
})
