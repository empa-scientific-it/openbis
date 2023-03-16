import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import RoleControllerChange from '@src/js/components/users/form/common/RoleControllerChange.js'
import UserFormControllerRecalculateInheritedRoles from '@src/js/components/users/form/user/UserFormControllerRecalculateInheritedRoles.js'
import UserFormSelectionType from '@src/js/components/users/form/user/UserFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === UserFormSelectionType.USER) {
      await this._handleChangeUser(params)
    } else if (type === UserFormSelectionType.GROUP) {
      await this._handleChangeGroup(params)
    } else if (type === UserFormSelectionType.ROLE) {
      await new RoleControllerChange(this.controller).execute(params)
    }
  }

  async _handleChangeUser(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.user,
        params.field,
        params.value
      )
      return {
        user: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeGroup(params) {
    await this.context.setState(state => {
      const newState = { ...state }

      const { newCollection, oldObject, newObject } =
        FormUtil.changeCollectionItemField(
          state.groups,
          params.id,
          params.field,
          params.value
        )
      newState.groups = newCollection

      this._handleChangeGroupCode(oldObject, newObject, newState)

      return newState
    })

    if (this.controller.groupsGridController) {
      await this.controller.groupsGridController.load()
      await this.controller.groupsGridController.showRow(params.id)
    }

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.load()
    }

    await this.controller.changed(true)
  }

  _handleChangeGroupCode(oldGroup, newGroup, newState) {
    const oldCode = oldGroup.code.value
    const newCode = newGroup.code.value

    if (oldCode !== newCode) {
      const { groups: groupDefinitions } = this.controller.getDictionaries()

      const groupDefinition = groupDefinitions.find(
        groupDefinition => groupDefinition.code === newCode
      )

      _.assign(newGroup, {
        description: {
          ...newGroup.description,
          value: groupDefinition.description
        },
        registrator: {
          ...newGroup.registrator,
          value: groupDefinition.registrator
            ? groupDefinition.registrator.userId
            : null
        },
        registrationDate: {
          ...newGroup.registrationDate,
          value: groupDefinition.registrationDate
        },
        modificationDate: {
          ...newGroup.modificationDate,
          value: groupDefinition.modificationDate
        }
      })

      new UserFormControllerRecalculateInheritedRoles(this.controller).execute(
        newState
      )
    }
  }
}
