import RoleControllerAdd from '@src/js/components/users/form/common/RoleControllerAdd.js'

export default class UserFormControllerAddRole {
  constructor(controller) {
    this.controller = controller
  }

  execute() {
    new RoleControllerAdd(this.controller).execute()
  }
}
