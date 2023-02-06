import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('add role', testAddRole)
})

async function testAddRole() {
  const {
    mySpace,
    testProject,
    instanceObserverGroup,
    instanceObserverGroupAssignment,
    mySpaceAdminAssignment
  } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([mySpaceAdminAssignment])

  common.facade.loadSpaces.mockReturnValue(
    Promise.resolve([mySpace, testProject.space])
  )
  common.facade.loadProjects.mockReturnValue(Promise.resolve([testProject]))
  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )

  const form = await common.mountExisting(user)

  form.expectJSON({
    groupsGrid: {
      rows: [
        {
          values: {
            code: instanceObserverGroup.getCode(),
            description: instanceObserverGroup.getDescription()
          },
          selected: false
        }
      ]
    },
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: instanceObserverGroup.getCode(),
            level: instanceObserverGroupAssignment.getRoleLevel(),
            space: '(All)',
            project: '(All)',
            role: instanceObserverGroupAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
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
    groupsGrid: {
      rows: [
        {
          values: {
            code: instanceObserverGroup.getCode(),
            description: instanceObserverGroup.getDescription()
          },
          selected: false
        }
      ]
    },
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: instanceObserverGroup.getCode(),
            level: instanceObserverGroupAssignment.getRoleLevel(),
            space: '(All)',
            project: '(All)',
            role: instanceObserverGroupAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            inheritedFrom: null,
            level: null,
            space: null,
            project: null,
            role: null
          },
          selected: true
        },
        {
          values: {
            inheritedFrom: null,
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
      addGroup: {
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

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.PROJECT)
  await form.update()

  form.getParameters().getRole().getSpace().change(testProject.space.code)
  await form.update()

  form.getParameters().getRole().getProject().change(testProject.code)
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [
        {
          values: {
            code: instanceObserverGroup.getCode(),
            description: instanceObserverGroup.getDescription()
          },
          selected: false
        }
      ]
    },
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: instanceObserverGroup.getCode(),
            level: instanceObserverGroupAssignment.getRoleLevel(),
            space: '(All)',
            project: '(All)',
            role: instanceObserverGroupAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: false
        },
        {
          values: {
            inheritedFrom: null,
            level: openbis.RoleLevel.PROJECT,
            space: testProject.space.code,
            project: testProject.code,
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
          value: openbis.RoleLevel.PROJECT,
          enabled: true,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: testProject.space.code,
          enabled: true,
          mode: 'edit'
        },
        project: {
          label: 'Project',
          value: testProject.code,
          enabled: true,
          mode: 'edit'
        },
        role: {
          label: 'Role',
          value: openbis.Role.ADMIN,
          enabled: true,
          mode: 'edit'
        }
      }
    },
    buttons: {
      addGroup: {
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
