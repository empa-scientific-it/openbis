import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('change role', testChangeRole)
})

async function testChangeRole() {
  const { mySpace, myProject, instanceAdminAssignment } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([instanceAdminAssignment])

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([mySpace]))
  common.facade.loadProjects.mockReturnValue(Promise.resolve([myProject]))
  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  form.getRolesGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: instanceAdminAssignment.getRoleLevel(),
            space: '(All)',
            project: '(All)',
            role: instanceAdminAssignment.getRole()
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
          value: instanceAdminAssignment.getRoleLevel(),
          mode: 'edit'
        },
        space: null,
        project: null,
        role: {
          label: 'Role',
          value: instanceAdminAssignment.getRole(),
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.SPACE)
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: openbis.RoleLevel.SPACE,
            space: null,
            project: null,
            role: null
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
          value: openbis.RoleLevel.SPACE,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: null,
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: null,
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getRole().getSpace().change(mySpace.getCode())
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: openbis.RoleLevel.SPACE,
            space: mySpace.getCode(),
            project: '(All)',
            role: null
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
          value: openbis.RoleLevel.SPACE,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: mySpace.getCode(),
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: null,
          mode: 'edit'
        }
      }
    }
  })

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: openbis.RoleLevel.SPACE,
            space: mySpace.getCode(),
            project: '(All)',
            role: openbis.Role.ADMIN
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
          value: openbis.RoleLevel.SPACE,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: mySpace.getCode(),
          mode: 'edit'
        },
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
