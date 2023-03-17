import _ from 'lodash'
import PageControllerChange from '@src/js/components/common/page/PageControllerChange.js'
import RoleControllerChange from '@src/js/components/users/form/common/RoleControllerChange.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/usergroup/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserGroupFormControllerChange extends PageControllerChange {
  async execute(type, params) {
    if (type === UserGroupFormSelectionType.GROUP) {
      await this._handleChangeGroup(params)
    } else if (type === UserGroupFormSelectionType.USER) {
      await this._handleChangeUser(params)
    } else if (type === UserGroupFormSelectionType.ROLE) {
      await new RoleControllerChange(this.controller).execute(params)
    }
  }

  async _handleChangeGroup(params) {
    await this.context.setState(state => {
      const { newObject } = FormUtil.changeObjectField(
        state.group,
        params.field,
        params.value
      )
      return {
        group: newObject
      }
    })
    await this.controller.changed(true)
  }

  async _handleChangeUser(params) {
    await this.context.setState(state => {
      const newState = { ...state }

      const { newCollection, oldObject, newObject } =
        FormUtil.changeCollectionItemField(
          state.users,
          params.id,
          params.field,
          params.value
        )
      newState.users = newCollection

      this._handleChangeUserId(oldObject, newObject)

      return newState
    })

    if (this.controller.usersGridController) {
      await this.controller.usersGridController.load()
      await this.controller.usersGridController.showRow(params.id)
    }

    await this.controller.changed(true)
  }

  _handleChangeUserId(oldUser, newUser) {
    const oldUserId = oldUser.userId.value
    const newUserId = newUser.userId.value

    if (oldUserId !== newUserId) {
      const { users } = this.controller.getDictionaries()

      const user = users.find(user => user.userId === newUserId)

      _.assign(newUser, {
        firstName: {
          ...newUser.firstName,
          value: user.firstName
        },
        lastName: {
          ...newUser.lastName,
          value: user.lastName
        },
        email: {
          ...newUser.email,
          value: user.email
        },
        space: {
          ...newUser.space,
          value: user.space ? user.space.code : null
        },
        active: {
          ...newUser.active,
          value: user.active
        },
        registrator: {
          ...newUser.registrator,
          value: user.registrator ? user.registrator.userId : null
        },
        registrationDate: {
          ...newUser.registrationDate,
          value: user.registrationDate
        }
      })
    }
  }
}
