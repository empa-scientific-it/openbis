import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class PropertyTypeFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()
    const propertyType = this._preparePropertyType(state.propertyType)
    const operations = []

    if (propertyType.original) {
      operations.push(this._updatePropertyTypeOperation(propertyType))
    } else {
      operations.push(this._createPropertyTypeOperation(propertyType))
    }

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return propertyType.code.value
  }

  _preparePropertyType(propertyType) {
    const code = propertyType.code.value
    return FormUtil.trimFields({
      ...propertyType,
      code: {
        value: code ? code.toUpperCase() : null
      }
    })
  }

  _createPropertyTypeOperation(propertyType) {
    const creation = new openbis.PropertyTypeCreation()
    creation.setCode(propertyType.code.value)
    creation.setManagedInternally(propertyType.internal.value)
    creation.setLabel(propertyType.label.value)
    creation.setDescription(propertyType.description.value)
    creation.setDataType(propertyType.dataType.value)
    creation.setSchema(propertyType.schema.value)
    creation.setTransformation(propertyType.transformation.value)

    if (
      propertyType.dataType.value === openbis.DataType.CONTROLLEDVOCABULARY &&
      propertyType.vocabulary.value
    ) {
      creation.setVocabularyId(
        new openbis.VocabularyPermId(propertyType.vocabulary.value)
      )
    }
    if (
      propertyType.dataType.value === openbis.DataType.MATERIAL &&
      propertyType.materialType.value
    ) {
      creation.setMaterialTypeId(
        new openbis.EntityTypePermId(
          propertyType.materialType.value,
          openbis.EntityKind.MATERIAL
        )
      )
    }
    if (
      propertyType.dataType.value === openbis.DataType.SAMPLE &&
      propertyType.sampleType.value
    ) {
      creation.setSampleTypeId(
        new openbis.EntityTypePermId(
          propertyType.sampleType.value,
          openbis.EntityKind.SAMPLE
        )
      )
    }
    return new openbis.CreatePropertyTypesOperation([creation])
  }

  _updatePropertyTypeOperation(propertyType) {
    const update = new openbis.PropertyTypeUpdate()
    update.setTypeId(new openbis.PropertyTypePermId(propertyType.code.value))
    update.setLabel(propertyType.label.value)
    update.setDescription(propertyType.description.value)
    update.setSchema(propertyType.schema.value)
    update.setTransformation(propertyType.transformation.value)
    update.convertToDataType(propertyType.dataType.value)
    return new openbis.UpdatePropertyTypesOperation([update])
  }
}
