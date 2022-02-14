import PageController from '@src/js/components/common/page/PageController.js'
import PropertyTypeFormControllerLoad from '@src/js/components/types/form/propertytype/PropertyTypeFormControllerLoad.js'
import PropertyTypeFormControllerValidate from '@src/js/components/types/form/propertytype/PropertyTypeFormControllerValidate.js'
import PropertyTypeFormControllerChange from '@src/js/components/types/form/propertytype/PropertyTypeFormControllerChange.js'
import PropertyTypeFormControllerSave from '@src/js/components/types/form/propertytype/PropertyTypeFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class PropertyTypeFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.TYPES
  }

  getNewObjectType() {
    return objectTypes.NEW_PROPERTY_TYPE
  }

  getExistingObjectType() {
    return objectTypes.PROPERTY_TYPE
  }

  load() {
    return new PropertyTypeFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new PropertyTypeFormControllerValidate(this).execute(autofocus)
  }

  handleChange(type, params) {
    return new PropertyTypeFormControllerChange(this).execute(type, params)
  }

  handleSave() {
    return new PropertyTypeFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
