import RoleControllerRemove from '@src/js/components/users/form/common/RoleControllerRemove.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'

export default class UserGroupFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === UserGroupFormSelectionType.USER) {
      this._handleRemoveUser(selection.params.id)
    } else if (selection.type === UserGroupFormSelectionType.ROLE) {
      new RoleControllerRemove(this.controller).execute(selection.params.id)
    }
  }

  async _handleRemoveUser(userId) {
    const { users } = this.context.getState()

    const userIndex = users.findIndex(user => user.id === userId)

    const newUsers = Array.from(users)
    newUsers.splice(userIndex, 1)

    await this.context.setState(state => ({
      ...state,
      users: newUsers,
      selection: null
    }))

    if (this.controller.usersGridController) {
      await this.controller.usersGridController.selectRow(null)
      await this.controller.usersGridController.load()
    }

    await this.controller.changed(true)
  }
}
