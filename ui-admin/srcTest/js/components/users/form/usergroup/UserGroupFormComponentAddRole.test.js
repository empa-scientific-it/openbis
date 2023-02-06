import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('add role', testAddRole)
})

async function testAddRole() {
  const {
    mySpace,
    myProject,
    testSpace,
    testProject,
    instanceObserverAssignment,
    testProjectAdminAssignment,
    mySpaceAdminAssignment
  } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setRoleAssignments([
    instanceObserverAssignment,
    testProjectAdminAssignment,
    mySpaceAdminAssignment
  ])

  common.facade.loadSpaces.mockReturnValue(
    Promise.resolve([mySpace, testSpace])
  )
  common.facade.loadProjects.mockReturnValue(
    Promise.resolve([myProject, testProject])
  )

  const form = await common.mountExisting(group)

  form.expectJSON({
    usersGrid: {
      rows: []
    },
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
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            level: testProjectAdminAssignment.getRoleLevel(),
            space: testProjectAdminAssignment.project.space.code,
            project: testProjectAdminAssignment.project.code,
            role: testProjectAdminAssignment.getRole()
          },
          selected: false
        }
      ]
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddRole().click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: []
    },
    rolesGrid: {
      rows: [
        {
          values: {
            level: null,
            space: null,
            project: null,
            role: null
          },
          selected: true
        },
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
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            level: testProjectAdminAssignment.getRoleLevel(),
            space: testProjectAdminAssignment.project.space.code,
            project: testProjectAdminAssignment.project.code,
            role: testProjectAdminAssignment.getRole()
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
          value: null,
          enabled: true,
          mode: 'edit'
        },
        space: null,
        project: null,
        role: null
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

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.SPACE)
  await form.update()

  form.getParameters().getRole().getSpace().change(testSpace.code)
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: []
    },
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
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            level: openbis.RoleLevel.SPACE,
            space: testSpace.code,
            project: '(All)',
            role: openbis.Role.ADMIN
          },
          selected: true
        },
        {
          values: {
            level: testProjectAdminAssignment.getRoleLevel(),
            space: testProjectAdminAssignment.project.space.code,
            project: testProjectAdminAssignment.project.code,
            role: testProjectAdminAssignment.getRole()
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
          value: openbis.RoleLevel.SPACE,
          enabled: true,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: testSpace.code,
          enabled: true,
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: openbis.Role.ADMIN,
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
