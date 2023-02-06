import PageController from '@src/js/components/common/page/PageController.js'
import PluginFormControllerLoad from '@src/js/components/tools/form/plugin/PluginFormControllerLoad.js'
import PluginFormControllerValidate from '@src/js/components/tools/form/plugin/PluginFormControllerValidate.js'
import PluginFormControllerChange from '@src/js/components/tools/form/plugin/PluginFormControllerChange.js'
import PluginFormControllerEvaluate from '@src/js/components/tools/form/plugin/PluginFormControllerEvaluate.js'
import PluginFormControllerSave from '@src/js/components/tools/form/plugin/PluginFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class PluginFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.TOOLS
  }

  isDynamicPropertyType() {
    return (
      this.object.type === objectTypes.DYNAMIC_PROPERTY_PLUGIN ||
      this.object.type === objectTypes.NEW_DYNAMIC_PROPERTY_PLUGIN
    )
  }

  isEntityValidationType() {
    return (
      this.object.type === objectTypes.ENTITY_VALIDATION_PLUGIN ||
      this.object.type === objectTypes.NEW_ENTITY_VALIDATION_PLUGIN
    )
  }

  getNewObjectType() {
    if (this.isDynamicPropertyType()) {
      return objectTypes.NEW_DYNAMIC_PROPERTY_PLUGIN
    } else if (this.isEntityValidationType()) {
      return objectTypes.NEW_ENTITY_VALIDATION_PLUGIN
    } else {
      throw new Error('Unsupported object type: ' + this.object.type)
    }
  }

  getExistingObjectType() {
    if (this.isDynamicPropertyType()) {
      return objectTypes.DYNAMIC_PROPERTY_PLUGIN
    } else if (this.isEntityValidationType()) {
      return objectTypes.ENTITY_VALIDATION_PLUGIN
    } else {
      throw new Error('Unsupported object type: ' + this.object.type)
    }
  }

  load() {
    return new PluginFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new PluginFormControllerValidate(this).execute(autofocus)
  }

  handleChange(type, params) {
    return new PluginFormControllerChange(this).execute(type, params)
  }

  handleEvaluate() {
    return new PluginFormControllerEvaluate(this).execute()
  }

  handleSave() {
    return new PluginFormControllerSave(this).execute()
  }
}
