import RoleControllerRemove from '@src/js/components/users/form/common/RoleControllerRemove.js'
import UserFormControllerRecalculateInheritedRoles from '@src/js/components/users/form/UserFormControllerRecalculateInheritedRoles.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'

export default class UserFormControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute() {
    const { selection } = this.context.getState()
    if (selection.type === UserFormSelectionType.GROUP) {
      this._handleRemoveGroup(selection.params.id)
    } else if (selection.type === UserFormSelectionType.ROLE) {
      new RoleControllerRemove(this.controller).execute(selection.params.id)
    }
  }

  async _handleRemoveGroup(groupId) {
    const { groups } = this.context.getState()

    const groupIndex = groups.findIndex(group => group.id === groupId)

    const newGroups = Array.from(groups)
    newGroups.splice(groupIndex, 1)

    await this.context.setState(state => {
      const newState = { ...state, groups: newGroups, selection: null }
      new UserFormControllerRecalculateInheritedRoles(this.controller).execute(
        newState
      )
      return newState
    })

    if (this.controller.groupsGridController) {
      await this.controller.groupsGridController.selectRow(null)
      await this.controller.groupsGridController.load()
    }

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.load()
    }

    await this.controller.changed(true)
  }
}
