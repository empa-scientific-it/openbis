import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import GridWrapper from '@srcTest/js/components/common/grid/wrapper/GridWrapper.js'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import MessageWrapper from '@srcTest/js/components/common/form/wrapper/MessageWrapper.js'
import Message from '@src/js/components/common/form/Message.jsx'
import ids from '@src/js/common/consts/ids.js'

export default class UserSearchWrapper extends BaseWrapper {
  getMessages() {
    const messages = []
    this.findComponent(Message).forEach(message => {
      messages.push(new MessageWrapper(message))
    })
    return messages
  }

  getUsers() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.USERS_GRID_ID
      })
    )
  }

  getUsersRoles() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.ROLES_OF_USERS_GRID_ID
      })
    )
  }

  getUserGroups() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.GROUPS_GRID_ID
      })
    )
  }

  getUserGroupsRoles() {
    return new GridWrapper(
      this.findComponent(Grid).filter({
        id: ids.ROLES_OF_GROUPS_GRID_ID
      })
    )
  }

  toJSON() {
    return {
      messages: this.getMessages().map(message => message.toJSON()),
      users: this.getUsers().toJSON(),
      usersRoles: this.getUsersRoles().toJSON(),
      userGroups: this.getUserGroups().toJSON(),
      userGroupsRoles: this.getUserGroupsRoles().toJSON()
    }
  }
}
