import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class PropertyTypeFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    await this.context.setState(oldState => {
      const { oldObject, newObject } = FormUtil.changeObjectField(
        oldState.propertyType,
        params.field,
        params.value
      )

      this._handleChangePropertyDataType(oldObject, newObject)

      return {
        propertyType: newObject
      }
    })
    await this.controller.changed(true)
  }

  _handleChangePropertyDataType(oldProperty, newProperty) {
    const oldDataType = oldProperty.dataType.value
    const newDataType = newProperty.dataType.value

    if (oldDataType !== newDataType) {
      _.assign(newProperty, {
        vocabulary: {
          ...newProperty.vocabulary,
          visible: newDataType === openbis.DataType.CONTROLLEDVOCABULARY
        },
        materialType: {
          ...newProperty.materialType,
          visible: newDataType === openbis.DataType.MATERIAL
        },
        sampleType: {
          ...newProperty.sampleType,
          visible: newDataType === openbis.DataType.SAMPLE
        },
        schema: {
          ...newProperty.schema,
          visible: newDataType === openbis.DataType.XML
        },
        transformation: {
          ...newProperty.transformation,
          visible: newDataType === openbis.DataType.XML
        }
      })
    }
  }
}
