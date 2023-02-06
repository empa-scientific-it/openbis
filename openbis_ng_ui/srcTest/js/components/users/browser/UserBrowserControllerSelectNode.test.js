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
  test('select node', testSelectNode)
  test('select another node', testSelectAnotherNode)
  test('select virtual node', testSelectVirtualNode)
})

async function testSelectNode() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([
    fixture.TEST_USER_GROUP_DTO,
    fixture.ANOTHER_USER_GROUP_DTO,
    fixture.ALL_USERS_GROUP_DTO
  ])

  await common.controller.load()
  await common.controller.changeAutoShowSelectedObject()

  await common.controller.selectObject({
    type: objectType.USER,
    id: fixture.TEST_USER_DTO.userId
  })

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
      {
        id: 'root__user',
        text: 'Users',
        expanded: false,
        selected: false,
        children: []
      },
      {
        id: 'root__userGroup',
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ]
  })

  await common.controller.expandNode('root__user')

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
      {
        id: 'root__user',
        text: 'Users',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_USER_DTO.userId,
            expanded: false,
            selected: false
          },
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: true
          }
        ]
      },
      {
        id: 'root__userGroup',
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ]
  })
}

async function testSelectAnotherNode() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([])

  await common.controller.load()
  await common.controller.changeAutoShowSelectedObject()

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
      {
        id: 'root__user',
        text: 'Users',
        expanded: false,
        selected: false,
        children: []
      },
      {
        id: 'root__userGroup',
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ]
  })

  await common.controller.expandNode('root__user')

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
      {
        id: 'root__user',
        text: 'Users',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_USER_DTO.userId,
            expanded: false,
            selected: false
          },
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: false
          }
        ]
      },
      {
        id: 'root__userGroup',
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ]
  })

  await common.controller.selectObject({
    type: objectType.USER,
    id: fixture.TEST_USER_DTO.userId
  })

  await common.controller.selectObject({
    type: objectType.USER,
    id: fixture.ANOTHER_USER_DTO.userId
  })

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
      {
        id: 'root__user',
        text: 'Users',
        expanded: true,
        selected: false,
        children: [
          {
            text: fixture.ANOTHER_USER_DTO.userId,
            expanded: false,
            selected: true
          },
          {
            text: fixture.TEST_USER_DTO.userId,
            expanded: false,
            selected: false
          }
        ]
      },
      {
        id: 'root__userGroup',
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ]
  })
}

async function testSelectVirtualNode() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([])

  await common.controller.load()
  await common.controller.changeAutoShowSelectedObject()

  await common.controller.selectObject({
    type: objectType.OVERVIEW,
    id: objectType.USER
  })

  expect(common.controller.getTree()).toMatchObject({
    id: 'root',
    children: [
      {
        text: 'Users',
        expanded: false,
        selected: true,
        children: []
      },
      {
        text: 'Groups',
        expanded: false,
        selected: false,
        children: []
      }
    ]
  })
}
