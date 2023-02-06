import RoleSelectionType from '@src/js/components/users/form/common/RoleSelectionType.js'
import messages from '@src/js/common/messages.js'

export default class RoleControllerValidate {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  validate(validator, roles) {
    roles.forEach(role => {
      validator.validateNotEmpty(role, 'level', messages.get(messages.LEVEL))
      if (role.space.visible) {
        validator.validateNotEmpty(role, 'space', messages.get(messages.SPACE))
      }
      if (role.project.visible) {
        validator.validateNotEmpty(
          role,
          'project',
          messages.get(messages.PROJECT)
        )
      }
      if (role.role.visible) {
        validator.validateNotEmpty(role, 'role', messages.get(messages.ROLE))
      }
    })
    return validator.withErrors(roles)
  }

  async select(firstError) {
    await this.context.setState(state => ({
      ...state,
      selection: {
        type: RoleSelectionType.ROLE,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      }
    }))

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.showRow(firstError.object.id)
    }
  }
}
