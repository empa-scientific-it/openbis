import UserBrowserControllerTest from '@srcTest/js/components/users/browser/UserBrowserControllerTest.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new UserBrowserControllerTest()
  common.beforeEach()
})

describe(UserBrowserControllerTest.SUITE, () => {
  test('load', testLoad)
})

async function testLoad() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([
    fixture.TEST_USER_GROUP_DTO,
    fixture.ANOTHER_USER_GROUP_DTO,
    fixture.ALL_USERS_GROUP_DTO
  ])

  await common.controller.load()

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Users',
      expanded: false,
      selected: false
    },
    {
      text: 'Groups',
      expanded: false,
      selected: false
    }
  ])
}
