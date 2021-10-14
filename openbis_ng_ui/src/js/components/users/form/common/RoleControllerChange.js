import _ from 'lodash'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import openbis from '@src/js/services/openbis.js'

export default class RoleControllerChange {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute(params) {
    await this.context.setState(state => {
      const { newCollection, oldObject, newObject } =
        FormUtil.changeCollectionItemField(
          state.roles,
          params.id,
          params.field,
          params.value
        )

      this._handleChangeRoleLevel(oldObject, newObject)
      this._handleChangeRoleSpace(oldObject, newObject)

      return {
        roles: newCollection
      }
    })

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.load()
      await this.controller.rolesGridController.showRow(params.id)
    }

    await this.controller.changed(true)
  }

  _handleChangeRoleLevel(oldRole, newRole) {
    const oldLevel = oldRole.level.value
    const newLevel = newRole.level.value

    if (oldLevel !== newLevel) {
      _.assign(newRole, {
        space: {
          ...newRole.space,
          visible:
            newLevel === openbis.RoleLevel.SPACE ||
            newLevel === openbis.RoleLevel.PROJECT,
          value: null
        },
        project: {
          ...newRole.project,
          visible: newLevel === openbis.RoleLevel.PROJECT,
          value: null
        },
        role: {
          ...newRole.role,
          visible: newLevel !== null,
          value: null
        }
      })
    }
  }

  _handleChangeRoleSpace(oldRole, newRole) {
    const oldSpace = oldRole.space.value
    const newSpace = newRole.space.value

    if (oldSpace !== newSpace) {
      _.assign(newRole, {
        project: {
          ...newRole.project,
          value: null
        },
        role: {
          ...newRole.role,
          value: null
        }
      })
    }
  }
}
