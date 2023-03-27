import _ from 'lodash'
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
    common.controller.getNodes().find(node =>
      _.isEqual(node.object, {
        type: objectType.OVERVIEW,
        id: objectType.USER_GROUP
      })
    ).id
  )

  expect(common.controller.getTree()).toMatchObject({
    children: [
      {
        text: 'Users',
        object: {
          type: objectType.OVERVIEW,
          id: objectType.USER
        },
        expanded: false,
        selected: false
      },
      {
        text: 'Groups',
        object: {
          type: objectType.OVERVIEW,
          id: objectType.USER_GROUP
        },
        expanded: true,
        selected: false
      }
    ]
  })

  await common.controller.collapseNode(
    common.controller.getNodes().find(node =>
      _.isEqual(node.object, {
        type: objectType.OVERVIEW,
        id: objectType.USER_GROUP
      })
    ).id
  )

  expect(common.controller.getTree()).toMatchObject({
    children: [
      {
        text: 'Users',
        object: {
          type: objectType.OVERVIEW,
          id: objectType.USER
        },
        expanded: false,
        selected: false
      },
      {
        text: 'Groups',
        object: {
          type: objectType.OVERVIEW,
          id: objectType.USER_GROUP
        },
        expanded: false,
        selected: false
      }
    ]
  })
}
