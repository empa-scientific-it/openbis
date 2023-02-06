import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('remove user', testRemoveUser)
})

async function testRemoveUser() {
  const { mySpaceUser, testSpaceUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setUsers([mySpaceUser, testSpaceUser])

  common.facade.loadUsers.mockReturnValue(
    Promise.resolve([mySpaceUser, testSpaceUser])
  )

  const form = await common.mountExisting(group)

  form.expectJSON({
    usersGrid: {
      rows: [mySpaceUser, testSpaceUser].map(user => ({
        values: {
          userId: user.getUserId(),
          firstName: user.getFirstName(),
          lastName: user.getLastName(),
          email: user.getEmail(),
          space: user.space !== null ? user.space.code : null,
          active: user.active !== null ? String(user.active) : null
        },
        selected: false
      }))
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getUsersGrid().getRows()[0].click()
  await form.update()

  form.getButtons().getRemove().click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [testSpaceUser].map(user => ({
        values: {
          userId: user.getUserId(),
          firstName: user.getFirstName(),
          lastName: user.getLastName(),
          email: user.getEmail(),
          space: user.space !== null ? user.space.code : null,
          active: user.active !== null ? String(user.active) : null
        },
        selected: false
      }))
    },
    buttons: {
      addUser: {
        enabled: true
      },
      addRole: {
        enabled: true
      },
      remove: {
        enabled: false
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
