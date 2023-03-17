import _ from 'lodash'
import PageControllerLoad from '@src/js/components/common/page/PageControllerLoad.js'
import RoleControllerLoad from '@src/js/components/users/form/common/RoleControllerLoad.js'
import UserFormSelectionType from '@src/js/components/users/form/user/UserFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserFormControllerLoad extends PageControllerLoad {
  async load(object, isNew) {
    return Promise.all([
      this._loadDictionaries(),
      this._loadUser(object, isNew)
    ])
  }

  async _loadDictionaries() {
    const [groups, spaces, projects] = await Promise.all([
      this.facade.loadGroups(),
      this.facade.loadSpaces(),
      this.facade.loadProjects()
    ])

    await this.context.setState(() => ({
      dictionaries: {
        groups,
        spaces,
        projects
      }
    }))
  }

  async _loadUser(object, isNew) {
    let loadedUser = null
    let loadedGroups = null

    if (!isNew) {
      ;[loadedUser, loadedGroups] = await Promise.all([
        this.facade.loadUser(object.id),
        this.facade.loadUserGroups(object.id)
      ])
      if (!loadedUser) {
        return
      }
    }

    const user = this._createUser(loadedUser)

    const groups = []
    const roles = []

    if (loadedUser && loadedUser.roleAssignments) {
      const userRoles = new RoleControllerLoad(this.controller).createRoles(
        loadedUser.roleAssignments
      )
      roles.push(...userRoles)
    }

    if (loadedGroups) {
      loadedGroups.forEach(loadedGroup => {
        const group = this._createGroup(loadedGroup)
        groups.push(group)

        if (loadedGroup.roleAssignments) {
          const groupRoles = new RoleControllerLoad(
            this.controller
          ).createRoles(loadedGroup.roleAssignments)
          roles.push(...groupRoles)
        }
      })
    }

    const selection = this._createSelection(groups, roles)

    return this.context.setState({
      user,
      groups,
      roles,
      selection,
      original: {
        user: user.original,
        groups: groups.map(group => group.original),
        roles: roles.map(role => role.original)
      }
    })
  }

  _createUser(loadedUser) {
    const user = {
      id: _.get(loadedUser, 'userId', null),
      userId: FormUtil.createField({
        value: _.get(loadedUser, 'userId', null),
        enabled: loadedUser === null
      }),
      space: FormUtil.createField({
        value: _.get(loadedUser, 'space.code', null)
      }),
      firstName: FormUtil.createField({
        value: _.get(loadedUser, 'firstName', null),
        visible: loadedUser !== null,
        enabled: false
      }),
      lastName: FormUtil.createField({
        value: _.get(loadedUser, 'lastName', null),
        visible: loadedUser !== null,
        enabled: false
      }),
      email: FormUtil.createField({
        value: _.get(loadedUser, 'email', null),
        visible: loadedUser !== null,
        enabled: false
      }),
      active: FormUtil.createField({
        value: _.get(loadedUser, 'active', true),
        enabled: loadedUser !== null
      })
    }
    if (loadedUser) {
      user.original = _.cloneDeep(user)
    }
    return user
  }

  _createGroup(loadedGroup) {
    const group = {
      id: _.uniqueId('group-'),
      code: FormUtil.createField({
        value: _.get(loadedGroup, 'code', null)
      }),
      description: FormUtil.createField({
        value: _.get(loadedGroup, 'description', null)
      }),
      registrator: FormUtil.createField({
        value: _.get(loadedGroup, 'registrator.userId', null)
      }),
      registrationDate: FormUtil.createField({
        value: _.get(loadedGroup, 'registrationDate', null)
      }),
      modificationDate: FormUtil.createField({
        value: _.get(loadedGroup, 'modificationDate', null)
      })
    }
    group.original = _.cloneDeep(group)
    return group
  }

  _createSelection(newGroups, newRoles) {
    const { selection: oldSelection, groups: oldGroups } =
      this.context.getState()

    if (!oldSelection) {
      return null
    } else if (oldSelection.type === UserFormSelectionType.GROUP) {
      const oldGroup = _.find(
        oldGroups,
        oldGroup => oldGroup.id === oldSelection.params.id
      )
      const newGroup = _.find(
        newGroups,
        newGroup => newGroup.code.value === oldGroup.code.value
      )

      if (newGroup) {
        return {
          type: UserFormSelectionType.GROUP,
          params: {
            id: newGroup.id
          }
        }
      }
    } else if (oldSelection.type === UserFormSelectionType.ROLE) {
      return new RoleControllerLoad(this.controller).createSelection(newRoles)
    } else {
      return null
    }
  }
}
