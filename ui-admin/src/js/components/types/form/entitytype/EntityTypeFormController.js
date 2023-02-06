import PageController from '@src/js/components/common/page/PageController.js'
import EntityTypeFormControllerLoad from '@src/js/components/types/form/entitytype/EntityTypeFormControllerLoad.js'
import EntityTypeFormControllerValidate from '@src/js/components/types/form/entitytype/EntityTypeFormControllerValidate.js'
import EntityTypeFormControllerSave from '@src/js/components/types/form/entitytype/EntityTypeFormControllerSave.js'
import EntityTypeFormControllerRemove from '@src/js/components/types/form/entitytype/EntityTypeFormControllerRemove.js'
import EntityTypeFormControllerAddSection from '@src/js/components/types/form/entitytype/EntityTypeFormControllerAddSection.js'
import EntityTypeFormControllerAddProperty from '@src/js/components/types/form/entitytype/EntityTypeFormControllerAddProperty.js'
import EntityTypeFormControllerChange from '@src/js/components/types/form/entitytype/EntityTypeFormControllerChange.js'
import EntityTypeFormControllerOrderChange from '@src/js/components/types/form/entitytype/EntityTypeFormControllerOrderChange.js'
import EntityTypeFormControllerStrategies from '@src/js/components/types/form/entitytype/EntityTypeFormControllerStrategies.js'
import pages from '@src/js/common/consts/pages.js'

export default class EntityTypeFormController extends PageController {
  getPage() {
    return pages.TYPES
  }

  getNewObjectType() {
    const strategies = new EntityTypeFormControllerStrategies()
    return strategies.getStrategy(this.object.type).getNewObjectType()
  }

  getExistingObjectType() {
    const strategies = new EntityTypeFormControllerStrategies()
    return strategies.getStrategy(this.object.type).getExistingObjectType()
  }

  load() {
    return new EntityTypeFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new EntityTypeFormControllerValidate(this).execute(autofocus)
  }

  handleOrderChange(type, params) {
    new EntityTypeFormControllerOrderChange(this).execute(type, params)
  }

  handleChange(type, params) {
    new EntityTypeFormControllerChange(this).execute(type, params)
  }

  handleAddSection() {
    new EntityTypeFormControllerAddSection(this).execute()
  }

  handleAddProperty() {
    new EntityTypeFormControllerAddProperty(this).execute()
  }

  handleRemove() {
    new EntityTypeFormControllerRemove(this).executeRemove()
  }

  handleRemoveConfirm() {
    new EntityTypeFormControllerRemove(this).executeRemove(true)
  }

  handleRemoveCancel() {
    new EntityTypeFormControllerRemove(this).executeCancel()
  }

  handleSave() {
    return new EntityTypeFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
