export default class RoleControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute(roleId) {
    const { roles } = this.context.getState()

    const roleIndex = roles.findIndex(role => role.id === roleId)

    const newRoles = Array.from(roles)
    newRoles.splice(roleIndex, 1)

    await this.context.setState(state => ({
      ...state,
      roles: newRoles,
      selection: null
    }))

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.selectRow(null)
      await this.controller.rolesGridController.load()
    }

    await this.controller.changed(true)
  }
}
