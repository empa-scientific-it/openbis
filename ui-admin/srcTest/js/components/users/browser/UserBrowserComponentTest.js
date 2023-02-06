import React from 'react'
import ComponentTest from '@srcTest/js/components/common/ComponentTest.js'
import BrowserWrapper from '@srcTest/js/components/common/browser/wrapper/BrowserWrapper.js'
import UserBrowser from '@src/js/components/users/browser/UserBrowser.jsx'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

export default class UserBrowserComponentTest extends ComponentTest {
  static SUITE = 'UserBrowserComponent'

  constructor() {
    super(
      () => <UserBrowser />,
      wrapper => new BrowserWrapper(wrapper)
    )
  }

  async beforeEach() {
    super.beforeEach()

    openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
    openbis.mockSearchGroups([
      fixture.TEST_USER_GROUP_DTO,
      fixture.ANOTHER_USER_GROUP_DTO,
      fixture.ALL_USERS_GROUP_DTO
    ])
  }
}
