import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import UserForm from '@src/js/components/users/form/user/UserForm.jsx'
import UserFormWrapper from '@srcTest/js/components/users/form/user/wrapper/UserFormWrapper.js'
import UserFormController from '@src/js/components/users/form/user/UserFormController.js'
import UserFormFacade from '@src/js/components/users/form/user/UserFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/users/form/user/UserFormFacade')

export default class UserFormComponentTest extends ComponentTest {
  static SUITE = 'UserFormComponent'

  constructor() {
    super(
      object => <UserForm object={object} controller={this.controller} />,
      wrapper => new UserFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new UserFormFacade()
    this.controller = new UserFormController(this.facade)
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_USER
    })
  }

  async mountExisting(user) {
    this.facade.loadUser.mockReturnValue(Promise.resolve(user))

    return await this.mount({
      id: user.getUserId(),
      type: objectTypes.USER
    })
  }
}
