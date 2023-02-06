import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('select role', testSelectRole)
  test('select instance role', async () => {
    await testSelectInstanceRole(true)
    await testSelectInstanceRole(false)
  })
  test('select space role', async () => {
    await testSelectSpaceRole(true)
    await testSelectSpaceRole(false)
  })
  test('select project role', async () => {
    await testSelectProjectRole(true)
    await testSelectProjectRole(false)
  })
})

async function testSelectRole() {
  const { mySpace, mySpaceAdminAssignment, myProjectAdminAssignment } =
    UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([mySpaceAdminAssignment, myProjectAdminAssignment])

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([mySpace]))
  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.expectJSON({
    rolesGrid: {
      rows: [
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
            level: myProjectAdminAssignment.getRoleLevel(),
            space: myProjectAdminAssignment.project.space.code,
            project: myProjectAdminAssignment.project.code,
            role: myProjectAdminAssignment.getRole()
          },
          selected: false
        }
      ]
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: user.getUserId(),
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
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: true
        },
        {
          values: {
            inheritedFrom: null,
            level: myProjectAdminAssignment.getRoleLevel(),
            space: myProjectAdminAssignment.project.space.code,
            project: myProjectAdminAssignment.project.code,
            role: myProjectAdminAssignment.getRole()
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
          value: mySpaceAdminAssignment.getRoleLevel(),
          mode: 'view'
        },
        space: {
          label: 'Space',
          value: mySpaceAdminAssignment.space.code,
          mode: 'view'
        },
        project: null,
        role: {
          label: 'Role',
          value: mySpaceAdminAssignment.getRole(),
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
            inheritedFrom: null,
            level: mySpaceAdminAssignment.getRoleLevel(),
            space: mySpaceAdminAssignment.space.code,
            project: '(All)',
            role: mySpaceAdminAssignment.getRole()
          },
          selected: true
        },
        {
          values: {
            inheritedFrom: null,
            level: myProjectAdminAssignment.getRoleLevel(),
            space: myProjectAdminAssignment.project.space.code,
            project: myProjectAdminAssignment.project.code,
            role: myProjectAdminAssignment.getRole()
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
          value: mySpaceAdminAssignment.getRoleLevel(),
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: mySpaceAdminAssignment.space.code,
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: mySpaceAdminAssignment.getRole(),
          mode: 'edit'
        }
      }
    }
  })
}

async function testSelectInstanceRole(inherited) {
  const { instanceObserverGroupAssignment, instanceAdminAssignment } =
    UserFormTestData

  const userAssignment = inherited
    ? instanceObserverGroupAssignment
    : instanceAdminAssignment

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([userAssignment])

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
            inheritedFrom: inherited
              ? userAssignment.authorizationGroup.code
              : null,
            level: userAssignment.getRoleLevel(),
            space: '(All)',
            project: '(All)',
            role: userAssignment.getRole()
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
          value: userAssignment.getRoleLevel(),
          enabled: !inherited,
          mode: 'edit'
        },
        space: null,
        project: null,
        role: {
          label: 'Role',
          value: userAssignment.getRole(),
          enabled: !inherited,
          mode: 'edit'
        }
      }
    },
    buttons: {
      remove: {
        enabled: !inherited
      }
    }
  })
}

async function testSelectSpaceRole(inherited) {
  const {
    testSpace,
    mySpace,
    testSpacePowerUserGroupAssignment,
    mySpaceAdminAssignment
  } = UserFormTestData

  const userAssignment = inherited
    ? testSpacePowerUserGroupAssignment
    : mySpaceAdminAssignment

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([userAssignment])

  common.facade.loadSpaces.mockReturnValue(
    Promise.resolve([testSpace, mySpace])
  )
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
            inheritedFrom: inherited
              ? userAssignment.authorizationGroup.code
              : null,
            level: userAssignment.getRoleLevel(),
            space: userAssignment.space.code,
            project: '(All)',
            role: userAssignment.getRole()
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
          value: userAssignment.getRoleLevel(),
          enabled: !inherited,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: userAssignment.space.code,
          enabled: !inherited,
          mode: 'edit'
        },
        project: null,
        role: {
          label: 'Role',
          value: userAssignment.getRole(),
          enabled: !inherited,
          mode: 'edit'
        }
      }
    },
    buttons: {
      remove: {
        enabled: !inherited
      }
    }
  })
}

async function testSelectProjectRole(inherited) {
  const {
    testSpace,
    mySpace,
    testProject,
    myProject,
    testProjectAdminGroupAssignment,
    myProjectAdminAssignment
  } = UserFormTestData

  const userAssignment = inherited
    ? testProjectAdminGroupAssignment
    : myProjectAdminAssignment

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([userAssignment])

  common.facade.loadSpaces.mockReturnValue(
    Promise.resolve([testSpace, mySpace])
  )
  common.facade.loadProjects.mockReturnValue(
    Promise.resolve([testProject, myProject])
  )
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
            inheritedFrom: inherited
              ? userAssignment.authorizationGroup.code
              : null,
            level: userAssignment.getRoleLevel(),
            space: userAssignment.project.space.code,
            project: userAssignment.project.code,
            role: userAssignment.getRole()
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
          value: userAssignment.getRoleLevel(),
          enabled: !inherited,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: userAssignment.project.space.code,
          enabled: !inherited,
          mode: 'edit'
        },
        project: {
          label: 'Project',
          value: userAssignment.project.code,
          enabled: !inherited,
          mode: 'edit'
        },
        role: {
          label: 'Role',
          value: userAssignment.getRole(),
          enabled: !inherited,
          mode: 'edit'
        }
      }
    },
    buttons: {
      remove: {
        enabled: !inherited
      }
    }
  })
}
