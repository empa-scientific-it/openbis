import _ from 'lodash'
import RoleSelectionType from '@src/js/components/users/form/common/RoleSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class RoleControllerLoad {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  createRoles(loadedRoles) {
    return loadedRoles.map(loadedRole => this._createRole(loadedRole))
  }

  _createRole(loadedRole) {
    const inheritedFrom = _.get(loadedRole, 'authorizationGroup.code', null)
    const level = _.get(loadedRole, 'roleLevel', null)

    let space = null
    let project = null

    if (level === openbis.RoleLevel.SPACE) {
      space = _.get(loadedRole, 'space.code')
    } else if (level === openbis.RoleLevel.PROJECT) {
      space = _.get(loadedRole, 'project.space.code')
      project = _.get(loadedRole, 'project.code')
    }

    const role = {
      id: _.uniqueId('role-'),
      techId: FormUtil.createField({
        value: _.get(loadedRole, 'id.techId', null)
      }),
      inheritedFrom: FormUtil.createField({
        value: inheritedFrom
      }),
      level: FormUtil.createField({
        value: level,
        enabled: inheritedFrom === null
      }),
      space: FormUtil.createField({
        value: space,
        visible: space !== null,
        enabled: inheritedFrom === null
      }),
      project: FormUtil.createField({
        value: project,
        visible: project !== null,
        enabled: inheritedFrom === null
      }),
      role: FormUtil.createField({
        value: _.get(loadedRole, 'role', null),
        enabled: inheritedFrom === null
      })
    }
    role.original = _.cloneDeep(role)
    return role
  }

  createSelection(newRoles) {
    const { selection: oldSelection, roles: oldRoles } = this.context.getState()

    const oldRole = _.find(
      oldRoles,
      oldRole => oldRole.id === oldSelection.params.id
    )
    const newRole = _.find(newRoles, newRole =>
      this._areRolesEqual(newRole, oldRole)
    )

    if (newRole) {
      return {
        type: RoleSelectionType.ROLE,
        params: {
          id: newRole.id
        }
      }
    } else {
      return null
    }
  }

  _areRolesEqual(role1, role2) {
    const fields = [
      'inheritedFrom.value',
      'space.value',
      'project.value',
      'role.value'
    ]
    return fields.every(field => _.get(role1, field) === _.get(role2, field))
  }
}
