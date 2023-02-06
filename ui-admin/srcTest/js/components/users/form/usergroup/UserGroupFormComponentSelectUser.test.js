import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('select user', testSelectUser)
})

async function testSelectUser() {
  const { mySpaceUser, testSpaceUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setUsers([mySpaceUser, testSpaceUser])

  common.facade.loadUsers.mockReturnValue([mySpaceUser, testSpaceUser])

  const form = await common.mountExisting(group)

  form.expectJSON({
    usersGrid: {
      rows: [mySpaceUser, testSpaceUser].map(user => ({
        values: {
          userId: user.getUserId()
        },
        selected: false
      }))
    },
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: group.getCode(),
          mode: 'view'
        }
      }
    }
  })

  form.getUsersGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [mySpaceUser, testSpaceUser].map((user, index) => ({
        values: {
          userId: user.getUserId()
        },
        selected: index === 0
      }))
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: mySpaceUser.getUserId(),
          mode: 'view'
        }
      }
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [mySpaceUser, testSpaceUser].map((user, index) => ({
        values: {
          userId: user.getUserId()
        },
        selected: index === 0
      }))
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: mySpaceUser.getUserId(),
          mode: 'edit'
        }
      }
    }
  })
}
