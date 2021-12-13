import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import RoleControllerValidate from '@src/js/components/users/form/common/RoleControllerValidate.js'
import UserFormSelectionType from '@src/js/components/users/form/UserFormSelectionType.js'
import messages from '@src/js/common/messages.js'

export default class UserFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { user, groups, roles } = this.context.getState()

    const newUser = this._validateUser(validator, user)
    const newGroups = this._validateGroups(validator, groups)
    const newRoles = new RoleControllerValidate(this.controller).validate(
      validator,
      roles
    )

    return {
      user: newUser,
      groups: newGroups,
      roles: newRoles
    }
  }

  async select(firstError) {
    const { user, groups, roles } = this.context.getState()

    if (firstError.object === user) {
      await this.setSelection({
        type: UserFormSelectionType.USER,
        params: {
          part: firstError.name
        }
      })
    } else if (groups.includes(firstError.object)) {
      await this.setSelection({
        type: UserFormSelectionType.GROUP,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.groupsGridController) {
        await this.controller.groupsGridController.showRow(firstError.object.id)
      }
    } else if (roles.includes(firstError.object)) {
      await new RoleControllerValidate(this.controller).select(firstError)
    }
  }

  _validateUser(validator, user) {
    validator.validateNotEmpty(user, 'userId', messages.get(messages.USER_ID))
    validator.validateUserCode(user, 'userId', messages.get(messages.USER_ID))
    return validator.withErrors(user)
  }

  _validateGroups(validator, groups) {
    groups.forEach(group => {
      validator.validateNotEmpty(group, 'code', messages.get(messages.CODE))
    })
    return validator.withErrors(groups)
  }
}
