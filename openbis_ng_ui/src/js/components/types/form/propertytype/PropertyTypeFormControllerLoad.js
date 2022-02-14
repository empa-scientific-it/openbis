import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import users from '@src/js/common/consts/users.js'
import openbis from '@src/js/services/openbis.js'

export default class PropertyTypeFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(),
      this._loadPropertyType(object, isNew)
    ])
  }

  async _loadDictionaries() {
    const [vocabularies, materialTypes, sampleTypes] = await Promise.all([
      this.facade.loadVocabularies(),
      this.facade.loadMaterialTypes(),
      this.facade.loadSampleTypes()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        vocabularies,
        materialTypes,
        sampleTypes
      }
    }))
  }

  async _loadPropertyType(object, isNew) {
    let loadedPropertyType = null

    if (!isNew) {
      loadedPropertyType = await this.facade.loadPropertyType(object.id)
      if (!loadedPropertyType) {
        return
      }
    }

    const propertyType = this._createPropertyType(loadedPropertyType)

    return this.context.setState({
      propertyType: propertyType,
      original: {
        propertyType: propertyType.original
      }
    })
  }

  _createPropertyType(loadedPropertyType) {
    const internal = _.get(loadedPropertyType, 'managedInternally', false)
    const dataType = _.get(loadedPropertyType, 'dataType', null)

    const propertyType = {
      id: _.get(loadedPropertyType, 'code', null),
      code: FormUtil.createField({
        value: _.get(loadedPropertyType, 'code', null),
        enabled: loadedPropertyType === null
      }),
      label: FormUtil.createField({
        value: _.get(loadedPropertyType, 'label', null),
        enabled: !internal || this.isSystemUser()
      }),
      description: FormUtil.createField({
        value: _.get(loadedPropertyType, 'description', null),
        enabled: !internal || this.isSystemUser()
      }),
      dataType: FormUtil.createField({
        value: dataType,
        enabled: !internal || this.isSystemUser()
      }),
      internal: FormUtil.createField({
        value: internal,
        visible: this.isSystemUser(),
        enabled: loadedPropertyType === null && this.isSystemUser()
      }),
      schema: FormUtil.createField({
        value: _.get(loadedPropertyType, 'schema', null),
        visible: dataType === openbis.DataType.XML,
        enabled: !internal || this.isSystemUser()
      }),
      transformation: FormUtil.createField({
        value: _.get(loadedPropertyType, 'transformation', null),
        visible: dataType === openbis.DataType.XML,
        enabled: !internal || this.isSystemUser()
      }),
      vocabulary: FormUtil.createField({
        value: _.get(loadedPropertyType, 'vocabulary.code', null),
        visible: dataType === openbis.DataType.CONTROLLEDVOCABULARY,
        enabled: loadedPropertyType === null
      }),
      materialType: FormUtil.createField({
        value: _.get(loadedPropertyType, 'materialType.code', null),
        visible: dataType === openbis.DataType.MATERIAL,
        enabled: loadedPropertyType === null
      }),
      sampleType: FormUtil.createField({
        value: _.get(loadedPropertyType, 'sampleType.code', null),
        visible: dataType === openbis.DataType.SAMPLE,
        enabled: loadedPropertyType === null
      })
    }

    if (loadedPropertyType) {
      propertyType.original = _.cloneDeep(propertyType)
    }

    return propertyType
  }

  isSystemUser() {
    return (
      this.context.getProps().session &&
      this.context.getProps().session.userName === users.SYSTEM
    )
  }
}
