import _ from 'lodash'
import UserGroupFormSelectionType from '@src/js/components/users/form/usergroup/UserGroupFormSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class UserGroupFormControllerAddUser {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
    this.usersGridController = controller.usersGridController
  }

  async execute() {
    let { users } = this.context.getState()

    const newUser = {
      id: _.uniqueId('user-'),
      userId: FormUtil.createField({}),
      firstName: FormUtil.createField({}),
      lastName: FormUtil.createField({}),
      email: FormUtil.createField({}),
      space: FormUtil.createField({}),
      active: FormUtil.createField({}),
      registrator: FormUtil.createField({}),
      registrationDate: FormUtil.createField({}),
      original: null
    }

    const newUsers = Array.from(users)
    newUsers.push(newUser)

    await this.context.setState(state => ({
      ...state,
      users: newUsers,
      selection: {
        type: UserGroupFormSelectionType.USER,
        params: {
          id: newUser.id,
          part: 'userId'
        }
      }
    }))

    await this.controller.changed(true)

    if (this.usersGridController) {
      await this.usersGridController.load()
      await this.usersGridController.selectRow(newUser.id)
      await this.usersGridController.showRow(newUser.id)
    }
  }
}
