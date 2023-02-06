import _ from 'lodash'
import PageControllerSave from '@src/js/components/common/page/PageControllerSave.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserGroupFormControllerSave extends PageControllerSave {
  async save() {
    const state = this.context.getState()

    const group = this._prepareGroup(state.group)
    const users = state.users
    const roles = state.roles

    const operations = []

    if (group.original) {
      if (this._isGroupUpdateNeeded(group)) {
        operations.push(this._updateGroupOperation(group))
      }
    } else {
      operations.push(this._createGroupOperation(group))
    }

    state.original.users.forEach(originalUser => {
      const user = _.find(users, ['id', originalUser.id])
      if (!user) {
        operations.push(
          this._deleteUserAssignmentOperation(group, originalUser)
        )
      }
    })

    users.forEach(user => {
      if (user.original) {
        if (this._isUserAssignmentUpdateNeeded(user)) {
          operations.push(
            this._deleteUserAssignmentOperation(group, user.original)
          )
          operations.push(this._createUserAssignmentOperation(group, user))
        }
      } else {
        operations.push(this._createUserAssignmentOperation(group, user))
      }
    })

    state.original.roles.forEach(originalRole => {
      const role = _.find(roles, ['id', originalRole.id])
      if (!role) {
        operations.push(
          this._deleteRoleAssignmentOperation(group, originalRole)
        )
      }
    })

    roles.forEach(role => {
      if (role.original) {
        if (this._isRoleAssignmentUpdateNeeded(role)) {
          operations.push(
            this._deleteRoleAssignmentOperation(group, role.original)
          )
          operations.push(this._createRoleAssignmentOperation(group, role))
        }
      } else {
        operations.push(this._createRoleAssignmentOperation(group, role))
      }
    })

    const options = new openbis.SynchronousOperationExecutionOptions()
    options.setExecuteInOrder(true)
    await this.facade.executeOperations(operations, options)

    return group.code.value
  }

  _prepareGroup(group) {
    const code = group.code.value
    return FormUtil.trimFields({
      ...group,
      code: {
        value: code ? code.toUpperCase() : null
      }
    })
  }

  _isGroupUpdateNeeded(group) {
    return FormUtil.haveFieldsChanged(group, group.original, ['description'])
  }

  _isUserAssignmentUpdateNeeded(user) {
    return FormUtil.haveFieldsChanged(user, user.original, ['userId'])
  }

  _isRoleAssignmentUpdateNeeded(role) {
    return FormUtil.haveFieldsChanged(role, role.original, [
      'level',
      'space',
      'project',
      'role'
    ])
  }

  _createGroupOperation(group) {
    const creation = new openbis.AuthorizationGroupCreation()
    creation.setCode(group.code.value)
    creation.setDescription(group.description.value)
    return new openbis.CreateAuthorizationGroupsOperation([creation])
  }

  _updateGroupOperation(group) {
    const update = new openbis.AuthorizationGroupUpdate()
    update.setAuthorizationGroupId(
      new openbis.AuthorizationGroupPermId(group.code.value)
    )
    update.setDescription(group.description.value)
    return new openbis.UpdateAuthorizationGroupsOperation([update])
  }

  _createUserAssignmentOperation(group, user) {
    const update = new openbis.AuthorizationGroupUpdate()
    update.setAuthorizationGroupId(
      new openbis.AuthorizationGroupPermId(group.code.value)
    )
    update.getUserIds().add(new openbis.PersonPermId(user.userId.value))
    return new openbis.UpdateAuthorizationGroupsOperation([update])
  }

  _deleteUserAssignmentOperation(group, user) {
    const update = new openbis.AuthorizationGroupUpdate()
    update.setAuthorizationGroupId(
      new openbis.AuthorizationGroupPermId(group.code.value)
    )
    update.getUserIds().remove(new openbis.PersonPermId(user.userId.value))
    return new openbis.UpdateAuthorizationGroupsOperation([update])
  }

  _createRoleAssignmentOperation(group, role) {
    const creation = new openbis.RoleAssignmentCreation()
    creation.setAuthorizationGroupId(
      new openbis.AuthorizationGroupPermId(group.code.value)
    )
    creation.setRole(role.role.value)

    const level = role.level.value
    if (level === openbis.RoleLevel.SPACE) {
      creation.setSpaceId(new openbis.SpacePermId(role.space.value))
    } else if (level === openbis.RoleLevel.PROJECT) {
      creation.setProjectId(
        new openbis.ProjectIdentifier(
          '/' + role.space.value + '/' + role.project.value
        )
      )
    }

    return new openbis.CreateRoleAssignmentsOperation([creation])
  }

  _deleteRoleAssignmentOperation(group, role) {
    const id = new openbis.RoleAssignmentTechId(role.techId.value)
    const options = new openbis.RoleAssignmentDeletionOptions()
    options.setReason('deleted via ng_ui')
    return new openbis.DeleteRoleAssignmentsOperation([id], options)
  }
}
