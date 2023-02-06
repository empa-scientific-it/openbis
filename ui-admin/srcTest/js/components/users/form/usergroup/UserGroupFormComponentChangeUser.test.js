import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('change user', testChangeUser)
})

async function testChangeUser() {
  const { mySpaceUser, testSpaceUser, inactiveUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setUsers([mySpaceUser, inactiveUser])

  common.facade.loadUsers.mockReturnValue(
    Promise.resolve([mySpaceUser, testSpaceUser, inactiveUser])
  )

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getUsersGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [inactiveUser, mySpaceUser].map((user, index) => ({
        values: {
          userId: user.getUserId(),
          firstName: user.getFirstName(),
          lastName: user.getLastName(),
          email: user.getEmail(),
          space: user.space !== null ? user.space.code : null,
          active: user.active !== null ? String(user.active) : null
        },
        selected: index === 0
      }))
    },
    rolesGrid: {
      rows: []
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: inactiveUser.getUserId(),
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getUser().getUserId().change(testSpaceUser.getUserId())
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [mySpaceUser, testSpaceUser].map((user, index) => ({
        values: {
          userId: user.getUserId(),
          firstName: user.getFirstName(),
          lastName: user.getLastName(),
          email: user.getEmail(),
          space: user.space !== null ? user.space.code : null,
          active: user.active !== null ? String(user.active) : null
        },
        selected: index === 1
      }))
    },
    rolesGrid: {
      rows: []
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: testSpaceUser.getUserId(),
          mode: 'edit'
        }
      }
    }
  })
}
