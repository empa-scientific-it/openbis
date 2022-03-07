import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import UserGroupForm from '@src/js/components/users/form/usergroup/UserGroupForm.jsx'
import UserGroupFormWrapper from '@srcTest/js/components/users/form/usergroup/wrapper/UserGroupFormWrapper.js'
import UserGroupFormController from '@src/js/components/users/form/usergroup/UserGroupFormController.js'
import UserGroupFormFacade from '@src/js/components/users/form/usergroup/UserGroupFormFacade'
import objectTypes from '@src/js/common/consts/objectType.js'

jest.mock('@src/js/components/users/form/usergroup/UserGroupFormFacade')

export default class UserGroupFormComponentTest extends ComponentTest {
  static SUITE = 'UserGroupFormComponent'

  constructor() {
    super(
      object => <UserGroupForm object={object} controller={this.controller} />,
      wrapper => new UserGroupFormWrapper(wrapper)
    )
    this.facade = null
    this.controller = null
  }

  async beforeEach() {
    super.beforeEach()

    this.facade = new UserGroupFormFacade()
    this.controller = new UserGroupFormController(this.facade)
  }

  async mountNew() {
    return await this.mount({
      type: objectTypes.NEW_USER_GROUP
    })
  }

  async mountExisting(group) {
    this.facade.loadGroup.mockReturnValue(Promise.resolve(group))

    return await this.mount({
      id: group.getCode(),
      type: objectTypes.USER_GROUP
    })
  }
}
