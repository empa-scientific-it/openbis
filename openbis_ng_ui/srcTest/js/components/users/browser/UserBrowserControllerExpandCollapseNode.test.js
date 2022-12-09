import UserBrowserConsts from '@src/js/components/users/browser/UserBrowserConsts.js'
import UserBrowserControllerTest from '@srcTest/js/components/users/browser/UserBrowserControllerTest.js'
import objectType from '@src/js/common/consts/objectType.js'
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
  await common.controller.expandNode(
    UserBrowserConsts.nodeId(UserBrowserConsts.TYPE_ROOT, objectType.USER_GROUP)
  )

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
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
    ]
  })

  await common.controller.collapseNode(
    UserBrowserConsts.nodeId(UserBrowserConsts.TYPE_ROOT, objectType.USER_GROUP)
  )

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
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
    ]
  })
}
