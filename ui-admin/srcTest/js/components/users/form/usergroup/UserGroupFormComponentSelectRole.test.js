import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('select role', testSelectRole)
})

async function testSelectRole() {
  const { instanceObserverAssignment } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setRoleAssignments([instanceObserverAssignment])

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
        }
      ]
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

  form.getRolesGrid().getRows()[0].click()
  await form.update()

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
          selected: true
        }
      ]
    },
    parameters: {
      role: {
        title: 'Role',
        level: {
          label: 'Level',
          value: instanceObserverAssignment.getRoleLevel(),
          mode: 'view'
        },
        space: null,
        project: null,
        role: {
          label: 'Role',
          value: instanceObserverAssignment.getRole(),
          mode: 'view'
        }
      }
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

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
          selected: true
        }
      ]
    },
    parameters: {
      role: {
        title: 'Role',
        level: {
          label: 'Level',
          value: instanceObserverAssignment.getRoleLevel(),
          mode: 'edit'
        },
        space: null,
        project: null,
        role: {
          label: 'Role',
          value: instanceObserverAssignment.getRole(),
          mode: 'edit'
        }
      }
    }
  })
}
