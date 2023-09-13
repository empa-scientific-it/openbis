/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(['stjs', "util/Exceptions"], function (stjs, exceptions) {
  var ExperimentType = function () {}
  stjs.extend(
    ExperimentType,
    null,
    [],
    function (constructor, prototype) {
      prototype['@type'] = 'as.dto.experiment.ExperimentType'
      constructor.serialVersionUID = 1
      prototype.fetchOptions = null
      prototype.permId = null
      prototype.code = null
      prototype.description = null
      prototype.modificationDate = null
      prototype.propertyAssignments = null
      prototype.validationPlugin = null
      prototype.metaData = null;
      prototype.getPropertyAssignments = function () {
        if (
          this.getFetchOptions() &&
          this.getFetchOptions().hasPropertyAssignments()
        ) {
          return this.propertyAssignments
        } else {
          throw new exceptions.NotFetchedException(
            'Property assignments have not been fetched.'
          )
        }
      }
      prototype.setPropertyAssignments = function (propertyAssignments) {
        this.propertyAssignments = propertyAssignments
      }
      prototype.getValidationPlugin = function () {
        if (
          this.getFetchOptions() &&
          this.getFetchOptions().hasValidationPlugin()
        ) {
          return this.validationPlugin
        } else {
          throw new exceptions.NotFetchedException(
            'Validation plugin have not been fetched.'
          )
        }
      }
      prototype.setValidationPlugin = function (validationPlugin) {
        this.validationPlugin = validationPlugin
      }
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
      prototype.getCode = function () {
        return this.code
      }
      prototype.setCode = function (code) {
        this.code = code
      }
      prototype.getDescription = function () {
        return this.description
      }
      prototype.setDescription = function (description) {
        this.description = description
      }
      prototype.getModificationDate = function () {
        return this.modificationDate
      }
      prototype.setModificationDate = function (modificationDate) {
        this.modificationDate = modificationDate
      }
      prototype.getMetaData = function() {
        return this.metaData;
      };
      prototype.setMetaData = function(metaData) {
        this.metaData = metaData;
      };
      prototype.toString = function () {
        return this.getCode()
      }
    },
    {
      fetchOptions: 'ExperimentTypeFetchOptions',
      permId: 'EntityTypePermId',
      modificationDate: 'Date',
      propertyAssignments: {
        name: 'List',
        arguments: ['PropertyAssignment']
      },
      validationPlugin: 'Plugin',
      metaData: {
        name: "Map",
        arguments: ["String", "String"]
      }
    }
  )
  return ExperimentType
})
