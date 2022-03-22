import UserBrowserControllerTest from '@srcTest/js/components/users/browser/UserBrowserControllerTest.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new UserBrowserControllerTest()
  common.beforeEach()
})

describe(UserBrowserControllerTest.SUITE, () => {
  test('expand and collapse node', testExpandAndCollapseNode)
})

async function testExpandAndCollapseNode() {
  openbis.mockSearchPersons([])
  openbis.mockSearchGroups([fixture.TEST_USER_GROUP_DTO])

  await common.controller.load()
  common.controller.nodeExpand('groups')

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Users',
      expanded: false,
      selected: false
    },
    {
      text: 'Groups',
      expanded: true,
      selected: false
    }
  ])

  common.controller.nodeCollapse('groups')

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
