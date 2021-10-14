import PageControllerSelectionChange from '@src/js/components/common/page/PageControllerSelectionChange.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/UserGroupFormSelectionType.js'

export default class UserGroupFormControllerSelectionChange extends PageControllerSelectionChange {
  constructor(controller) {
    super(controller)
    this.context = controller.context
    this.usersGridController = controller.usersGridController
    this.rolesGridController = controller.rolesGridController
  }

  async execute(type, params) {
    super.execute(type, params)

    if (this.usersGridController) {
      await this.usersGridController.selectRow(
        type === UserGroupFormSelectionType.USER && params ? params.id : null
      )
    }

    if (this.rolesGridController) {
      await this.rolesGridController.selectRow(
        type === UserGroupFormSelectionType.ROLE && params ? params.id : null
      )
    }

    await this.context.setState({
      selectedUserRow: this.usersGridController.getSelectedRow(),
      selectedRoleRow: this.rolesGridController.getSelectedRow()
    })
  }
}
