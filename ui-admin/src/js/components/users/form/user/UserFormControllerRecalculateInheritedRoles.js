import _ from 'lodash'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class UserFormControllerRecalculateInheritedRoles {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute(state) {
    const { groups: groupsDefinitions } = this.controller.getDictionaries()
    const { groups, roles } = state

    let newRoles = []

    groups.forEach(group => {
      const groupDefinition = groupsDefinitions.find(
        groupDefinition => groupDefinition.code === group.code.value
      )

      if (!groupDefinition) {
        return
      }

      groupDefinition.roleAssignments.forEach(roleDefinition => {
        const level = _.get(roleDefinition, 'roleLevel', null)

        let space = null
        let project = null

        if (level === openbis.RoleLevel.SPACE) {
          space = _.get(roleDefinition, 'space.code')
        } else if (level === openbis.RoleLevel.PROJECT) {
          space = _.get(roleDefinition, 'project.space.code')
          project = _.get(roleDefinition, 'project.code')
        }

        const newRole = {
          id: _.uniqueId('role-'),
          inheritedFrom: FormUtil.createField({
            value: groupDefinition.code
          }),
          level: FormUtil.createField({
            value: level,
            enabled: false
          }),
          space: FormUtil.createField({
            value: space,
            visible:
              level === openbis.RoleLevel.SPACE ||
              level === openbis.RoleLevel.PROJECT,
            enabled: false
          }),
          project: FormUtil.createField({
            value: project,
            visible: level === openbis.RoleLevel.PROJECT,
            enabled: false
          }),
          role: FormUtil.createField({
            value: _.get(roleDefinition, 'role'),
            enabled: false
          })
        }

        if (
          _.some(newRoles, {
            inheritedFrom: { value: newRole.inheritedFrom.value },
            level: { value: newRole.level.value },
            space: { value: newRole.space.value },
            project: { value: newRole.project.value },
            role: { value: newRole.role.value }
          })
        ) {
          return
        }

        newRole.original = _.cloneDeep(newRole)
        newRoles.push(newRole)
      })
    })

    roles.forEach(role => {
      if (!role.inheritedFrom.value) {
        newRoles.push(role)
      }
    })

    _.assign(state, {
      roles: newRoles
    })
  }
}
