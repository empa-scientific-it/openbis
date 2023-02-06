import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('remove role', testRemoveRole)
})

async function testRemoveRole() {
  const {
    testSpace,
    instanceObserverAssignment,
    testSpacePowerUserAssignment
  } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setRoleAssignments([
    instanceObserverAssignment,
    testSpacePowerUserAssignment
  ])

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([testSpace]))

  const form = await common.mountExisting(group)

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            level: instanceObserverAssignment.getRoleLevel(),
            space: '(All)',
            project: '(All)',
            role: instanceObserverAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            level: testSpacePowerUserAssignment.getRoleLevel(),
            space: testSpacePowerUserAssignment.space.code,
            project: '(All)',
            role: testSpacePowerUserAssignment.getRole()
          },
          selected: false
        }
      ]
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getRolesGrid().getRows()[0].click()
  await form.update()

  form.getButtons().getRemove().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            level: testSpacePowerUserAssignment.getRoleLevel(),
            space: testSpacePowerUserAssignment.space.code,
            project: '(All)',
            role: testSpacePowerUserAssignment.getRole()
          },
          selected: false
        }
      ]
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
