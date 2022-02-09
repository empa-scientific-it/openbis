import PageControllerValidate from '@src/js/components/common/page/PageConrollerValidate.js'
import RoleControllerValidate from '@src/js/components/users/form/common/RoleControllerValidate.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/usergroup/UserGroupFormSelectionType.js'
import messages from '@src/js/common/messages.js'

export default class UserGroupFormControllerValidate extends PageControllerValidate {
  validate(validator) {
    const { group, users, roles } = this.context.getState()

    const newGroup = this._validateGroup(validator, group)
    const newUsers = this._validateUsers(validator, users)
    const newRoles = new RoleControllerValidate(this.controller).validate(
      validator,
      roles
    )

    return {
      group: newGroup,
      users: newUsers,
      roles: newRoles
    }
  }

  async select(firstError) {
    const { group, users, roles } = this.context.getState()

    if (firstError.object === group) {
      await this.setSelection({
        type: UserGroupFormSelectionType.GROUP,
        params: {
          part: firstError.name
        }
      })
    } else if (users.includes(firstError.object)) {
      await this.setSelection({
        type: UserGroupFormSelectionType.USER,
        params: {
          id: firstError.object.id,
          part: firstError.name
        }
      })

      if (this.controller.usersGridController) {
        await this.controller.usersGridController.showRow(firstError.object.id)
      }
    } else if (roles.includes(firstError.object)) {
      await new RoleControllerValidate(this.controller).select(firstError)
    }
  }

  _validateGroup(validator, group) {
    validator.validateNotEmpty(group, 'code', messages.get(messages.CODE))
    validator.validateCode(group, 'code', messages.get(messages.CODE))
    return validator.withErrors(group)
  }

  _validateUsers(validator, users) {
    users.forEach(user => {
      validator.validateNotEmpty(user, 'userId', messages.get(messages.USER_ID))
    })
    return validator.withErrors(users)
  }
}
