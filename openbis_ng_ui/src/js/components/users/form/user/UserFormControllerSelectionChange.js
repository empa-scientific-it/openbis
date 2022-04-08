import PageControllerSelectionChange from '@src/js/components/common/page/PageControllerSelectionChange.js'
import UserFormSelectionType from '@src/js/components/users/form/user/UserFormSelectionType.js'

export default class UserFormControllerSelectionChange extends PageControllerSelectionChange {
  constructor(controller) {
    super(controller)
    this.context = controller.context
    this.groupsGridController = controller.groupsGridController
    this.rolesGridController = controller.rolesGridController
  }

  async execute(type, params) {
    super.execute(type, params)

    if (this.groupsGridController) {
      await this.groupsGridController.selectRow(
        type === UserFormSelectionType.GROUP && params ? params.id : null
      )
    }

    if (this.rolesGridController) {
      await this.rolesGridController.selectRow(
        type === UserFormSelectionType.ROLE && params ? params.id : null
      )
    }

    await this.context.setState({
      selectedGroupRow: this.groupsGridController.getSelectedRow(),
      selectedRoleRow: this.rolesGridController.getSelectedRow()
    })
  }
}
