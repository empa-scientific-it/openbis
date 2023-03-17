import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import RoleControllerLoad from '@src/js/components/users/form/common/RoleControllerLoad.js'
import UserGroupFormSelectionType from '@src/js/components/users/form/usergroup/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserGroupFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(),
      this._loadGroup(object, isNew)
    ])
  }

  async _loadDictionaries() {
    const [users, spaces, projects] = await Promise.all([
      this.facade.loadUsers(),
      this.facade.loadSpaces(),
      this.facade.loadProjects()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        users,
        spaces,
        projects
      }
    }))
  }

  async _loadGroup(object, isNew) {
    let loadedGroup = null

    if (!isNew) {
      loadedGroup = await this.facade.loadGroup(object.id)
      if (!loadedGroup) {
        return
      }
    }

    const group = this._createGroup(loadedGroup)

    const users = []
    const roles = []

    if (loadedGroup && loadedGroup.roleAssignments) {
      const groupRoles = new RoleControllerLoad(this.controller).createRoles(
        loadedGroup.roleAssignments
      )
      roles.push(...groupRoles)
    }

    if (loadedGroup && loadedGroup.users) {
      loadedGroup.users.forEach(loadedUser => {
        const user = this._createUser(loadedUser)
        users.push(user)
      })
    }

    const selection = this._createSelection(users, roles)

    return this.context.setState({
      group,
      users,
      roles,
      selection,
      original: {
        group: group.original,
        users: users.map(user => user.original),
        roles: roles.map(role => role.original)
      }
    })
  }

  _createGroup(loadedGroup) {
    const group = {
      id: _.get(loadedGroup, 'code', null),
      code: FormUtil.createField({
        value: _.get(loadedGroup, 'code', null),
        enabled: loadedGroup === null
      }),
      description: FormUtil.createField({
        value: _.get(loadedGroup, 'description', null)
      })
    }
    if (loadedGroup) {
      group.original = _.cloneDeep(group)
    }
    return group
  }

  _createUser(loadedUser) {
    const user = {
      id: _.uniqueId('user-'),
      userId: FormUtil.createField({
        value: _.get(loadedUser, 'userId', null)
      }),
      firstName: FormUtil.createField({
        value: _.get(loadedUser, 'firstName', null)
      }),
      lastName: FormUtil.createField({
        value: _.get(loadedUser, 'lastName', null)
      }),
      email: FormUtil.createField({
        value: _.get(loadedUser, 'email', null)
      }),
      space: FormUtil.createField({
        value: _.get(loadedUser, 'space.code', null)
      }),
      active: FormUtil.createField({
        value: _.get(loadedUser, 'active', null)
      }),
      registrator: FormUtil.createField({
        value: _.get(loadedUser, 'registrator.userId', null)
      }),
      registrationDate: FormUtil.createField({
        value: _.get(loadedUser, 'registrationDate', null)
      })
    }
    user.original = _.cloneDeep(user)
    return user
  }

  _createSelection(newUsers, newRoles) {
    const { selection: oldSelection, users: oldUsers } = this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === UserGroupFormSelectionType.USER) {
      const oldUser = _.find(
        oldUsers,
        oldUser => oldUser.id === oldSelection.params.id
      )
      const newUser = _.find(
        newUsers,
        newUser => newUser.userId.value === oldUser.userId.value
      )

      if (newUser) {
        return {
          type: UserGroupFormSelectionType.USER,
          params: {
            id: newUser.id
          }
        }
      }
    } else if (oldSelection.type === UserGroupFormSelectionType.ROLE) {
      return new RoleControllerLoad(this.controller).createSelection(newRoles)
    } else {
      return null
    }
  }
}
