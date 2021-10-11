import PageController from '@src/js/components/common/page/PageController.js'
import UserGroupFormControllerLoad from '@src/js/components/users/form/UserGroupFormControllerLoad.js'
import UserGroupFormControllerAddUser from '@src/js/components/users/form/UserGroupFormControllerAddUser.js'
import UserGroupFormControllerAddRole from '@src/js/components/users/form/UserGroupFormControllerAddRole.js'
import UserGroupFormControllerRemove from '@src/js/components/users/form/UserGroupFormControllerRemove.js'
import UserGroupFormControllerValidate from '@src/js/components/users/form/UserGroupFormControllerValidate.js'
import UserGroupFormControllerChange from '@src/js/components/users/form/UserGroupFormControllerChange.js'
import UserGroupFormControllerSelectionChange from '@src/js/components/users/form/UserGroupFormControllerSelectionChange.js'
import UserGroupFormControllerSave from '@src/js/components/users/form/UserGroupFormControllerSave.js'
import pages from '@src/js/common/consts/pages.js'
import objectTypes from '@src/js/common/consts/objectType.js'

export default class UserGroupFormController extends PageController {
  constructor(facade) {
    super(facade)
  }

  getPage() {
    return pages.USERS
  }

  getNewObjectType() {
    return objectTypes.NEW_USER_GROUP
  }

  getExistingObjectType() {
    return objectTypes.USER_GROUP
  }

  load() {
    return new UserGroupFormControllerLoad(this).execute()
  }

  validate(autofocus) {
    return new UserGroupFormControllerValidate(this).execute(autofocus)
  }

  handleAddUser() {
    return new UserGroupFormControllerAddUser(this).execute()
  }

  handleAddRole() {
    return new UserGroupFormControllerAddRole(this).execute()
  }

  handleRemove() {
    return new UserGroupFormControllerRemove(this).execute()
  }

  handleChange(type, params) {
    return new UserGroupFormControllerChange(this).execute(type, params)
  }

  handleSelectionChange(type, params) {
    return new UserGroupFormControllerSelectionChange(this).execute(
      type,
      params
    )
  }

  handleSave() {
    return new UserGroupFormControllerSave(this).execute()
  }

  getDictionaries() {
    const { dictionaries } = this.context.getState()
    return dictionaries || {}
  }
}
