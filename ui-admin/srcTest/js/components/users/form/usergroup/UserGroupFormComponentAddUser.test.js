import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('add user', testAddUser)
})

async function testAddUser() {
  const { mySpaceUser, testSpaceUser, inactiveUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setDescription('Test Description')
  group.setUsers([mySpaceUser, inactiveUser])

  common.facade.loadUsers.mockReturnValue(
    Promise.resolve([mySpaceUser, testSpaceUser, inactiveUser])
  )

  const form = await common.mountExisting(group)

  form.expectJSON({
    usersGrid: {
      rows: [inactiveUser, mySpaceUser].map(user => ({
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
    rolesGrid: {
      rows: []
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddUser().click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [new openbis.Person(), inactiveUser, mySpaceUser].map(
        (user, index) => ({
          values: {
            userId: user.getUserId(),
            firstName: user.getFirstName(),
            lastName: user.getLastName(),
            email: user.getEmail(),
            space: user.space !== null ? user.space.code : null,
            active: user.active !== null ? String(user.active) : null
          },
          selected: index === 0
        })
      )
    },
    rolesGrid: {
      rows: []
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: null,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addUser: {
        enabled: true
      },
      addRole: {
        enabled: true
      },
      remove: {
        enabled: true
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

  form.getParameters().getUser().getUserId().change(testSpaceUser.getUserId())
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [inactiveUser, mySpaceUser, testSpaceUser].map((user, index) => ({
        values: {
          userId: user.getUserId(),
          firstName: user.getFirstName(),
          lastName: user.getLastName(),
          email: user.getEmail(),
          space: user.space !== null ? user.space.code : null,
          active: user.active !== null ? String(user.active) : null
        },
        selected: index === 2
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
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addUser: {
        enabled: true
      },
      addRole: {
        enabled: true
      },
      remove: {
        enabled: true
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
