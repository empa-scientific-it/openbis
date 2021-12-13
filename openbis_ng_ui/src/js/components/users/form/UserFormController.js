import PageController from '@src/js/components/common/page/PageController.js'
import UserFormControllerLoad from '@src/js/components/users/form/UserFormControllerLoad.js'
import UserFormControllerAddGroup from '@src/js/components/users/form/UserFormControllerAddGroup.js'
import UserFormControllerAddRole from '@src/js/components/users/form/UserFormControllerAddRole.js'
import UserFormControllerRemove from '@src/js/components/users/form/UserFormControllerRemove.js'
import UserFormControllerValidate from '@src/js/components/users/form/UserFormControllerValidate.js'
import UserFormControllerChange from '@src/js/components/users/form/UserFormControllerChange.js'
import UserFormControllerSelectionChange from '@src/js/components/users/form/UserFormControllerSelectionChange.js'
import UserFormControllerSave from '@src/js/components/users/form/UserFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class UserFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.USERS
  }

  getNewObjectType() {
    return objectTypes.NEW_USER
  }

  getExistingObjectType() {
    return objectTypes.USER
  }

  load() {
    return new UserFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new UserFormControllerValidate(this).execute(autofocus)
  }

  handleAddGroup() {
    return new UserFormControllerAddGroup(this).execute()
  }

  handleAddRole() {
    return new UserFormControllerAddRole(this).execute()
  }

  handleRemove() {
    return new UserFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new UserFormControllerChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    return new UserFormControllerSelectionChange(this).execute(type, params)
  }

  handleSave() {
    return new UserFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
