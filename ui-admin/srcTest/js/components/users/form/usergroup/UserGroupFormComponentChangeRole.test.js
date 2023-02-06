import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('change role', testChangeRole)
})

async function testChangeRole() {
  const {
    mySpace,
    testSpace,
    mySpaceAdminAssignment,
    testSpacePowerUserAssignment
  } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setRoleAssignments([
    mySpaceAdminAssignment,
    testSpacePowerUserAssignment
  ])

  common.facade.loadSpaces.mockReturnValue(
    Promise.resolve([mySpace, testSpace])
  )

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getRolesGrid().getRows()[1].click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
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
          selected: true
        }
      ]
    },
    parameters: {
      role: {
        title: 'Role',
        level: {
          label: 'Level',
          value: testSpacePowerUserAssignment.getRoleLevel(),
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: testSpacePowerUserAssignment.space.code,
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: testSpacePowerUserAssignment.getRole(),
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.INSTANCE)
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            level: openbis.RoleLevel.INSTANCE,
            space: '(All)',
            project: '(All)',
            role: openbis.Role.ADMIN
          },
          selected: true
        },
        {
          values: {
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: false
        }
      ]
    },
    parameters: {
      role: {
        title: 'Role',
        level: {
          label: 'Level',
          value: openbis.RoleLevel.INSTANCE,
          mode: 'edit'
        },
        space: null,
        project: null,
        role: {
          label: 'Role',
          value: openbis.Role.ADMIN,
          mode: 'edit'
        }
      }
    }
  })
}
