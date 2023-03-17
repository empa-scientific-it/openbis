import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('sort roles', testSortRoles)
})

async function testSortRoles() {
  const {
    instanceObserverGroup,
    instanceObserverGroupAssignment,
    testSpacePowerUserGroup,
    testSpacePowerUserGroupAssignment,
    testProjectAdminGroup,
    testProjectAdminGroupAssignment,
    instanceAdminAssignment,
    mySpaceAdminAssignment,
    myProjectAdminAssignment
  } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([
    instanceAdminAssignment,
    mySpaceAdminAssignment,
    myProjectAdminAssignment
  ])

  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([
      instanceObserverGroup,
      testSpacePowerUserGroup,
      testProjectAdminGroup
    ])
  )

  const form = await common.mountExisting(user)

  const instanceObserverGroupRow = {
    values: {
      inheritedFrom: instanceObserverGroup.getCode(),
      level: instanceObserverGroupAssignment.getRoleLevel(),
      space: '(All)',
      project: '(All)',
      role: instanceObserverGroupAssignment.getRole()
    }
  }

  const testProjectAdminGroupRow = {
    values: {
      inheritedFrom: testProjectAdminGroup.getCode(),
      level: testProjectAdminGroupAssignment.getRoleLevel(),
      space: testProjectAdminGroupAssignment.project.space.code,
      project: testProjectAdminGroupAssignment.project.code,
      role: testProjectAdminGroupAssignment.getRole()
    }
  }

  const testSpacePowerUserGroupRow = {
    values: {
      inheritedFrom: testSpacePowerUserGroup.getCode(),
      level: testSpacePowerUserGroupAssignment.getRoleLevel(),
      space: testSpacePowerUserGroupAssignment.space.code,
      project: '(All)',
      role: testSpacePowerUserGroupAssignment.getRole()
    }
  }

  const instanceAdminAssignmentRow = {
    values: {
      inheritedFrom: null,
      level: instanceAdminAssignment.getRoleLevel(),
      space: '(All)',
      project: '(All)',
      role: instanceAdminAssignment.getRole()
    }
  }

  const mySpaceAdminAssignmentRow = {
    values: {
      inheritedFrom: null,
      level: mySpaceAdminAssignment.getRoleLevel(),
      space: mySpaceAdminAssignment.space.code,
      project: '(All)',
      role: mySpaceAdminAssignment.getRole()
    }
  }

  const myProjectAdminAssignmentRow = {
    values: {
      inheritedFrom: null,
      level: myProjectAdminAssignment.getRoleLevel(),
      space: myProjectAdminAssignment.project.space.code,
      project: myProjectAdminAssignment.project.code,
      role: myProjectAdminAssignment.getRole()
    }
  }

  form.expectJSON({
    rolesGrid: {
      columns: [
        {
          name: 'inheritedFrom',
          sort: 'asc'
        },
        {
          name: 'level',
          sort: null
        },
        {
          name: 'space',
          sort: null
        },
        {
          name: 'project',
          sort: null
        },
        {
          name: 'role',
          sort: null
        },
        {
          name: 'registrator',
          sort: null
        },
        {
          name: 'registrationDate',
          sort: null
        }
      ],
      rows: [
        instanceObserverGroupRow,
        testProjectAdminGroupRow,
        testSpacePowerUserGroupRow,
        instanceAdminAssignmentRow,
        mySpaceAdminAssignmentRow,
        myProjectAdminAssignmentRow
      ]
    }
  })

  form.getRolesGrid().getColumns()[1].getLabel().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      columns: [
        {
          name: 'inheritedFrom',
          sort: null
        },
        {
          name: 'level',
          sort: 'asc'
        },
        {
          name: 'space',
          sort: null
        },
        {
          name: 'project',
          sort: null
        },
        {
          name: 'role',
          sort: null
        },
        {
          name: 'registrator',
          sort: null
        },
        {
          name: 'registrationDate',
          sort: null
        }
      ],
      rows: [
        instanceObserverGroupRow,
        instanceAdminAssignmentRow,
        testSpacePowerUserGroupRow,
        mySpaceAdminAssignmentRow,
        testProjectAdminGroupRow,
        myProjectAdminAssignmentRow
      ]
    }
  })

  form.getRolesGrid().getColumns()[2].getLabel().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      columns: [
        {
          name: 'inheritedFrom',
          sort: null
        },
        {
          name: 'level',
          sort: null
        },
        {
          name: 'space',
          sort: 'asc'
        },
        {
          name: 'project',
          sort: null
        },
        {
          name: 'role',
          sort: null
        },
        {
          name: 'registrator',
          sort: null
        },
        {
          name: 'registrationDate',
          sort: null
        }
      ],
      rows: [
        instanceObserverGroupRow,
        instanceAdminAssignmentRow,
        mySpaceAdminAssignmentRow,
        myProjectAdminAssignmentRow,
        testSpacePowerUserGroupRow,
        testProjectAdminGroupRow
      ]
    }
  })

  form.getRolesGrid().getColumns()[3].getLabel().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      columns: [
        {
          name: 'inheritedFrom',
          sort: null
        },
        {
          name: 'level',
          sort: null
        },
        {
          name: 'space',
          sort: null
        },
        {
          name: 'project',
          sort: 'asc'
        },
        {
          name: 'role',
          sort: null
        },
        {
          name: 'registrator',
          sort: null
        },
        {
          name: 'registrationDate',
          sort: null
        }
      ],
      rows: [
        instanceObserverGroupRow,
        instanceAdminAssignmentRow,
        testSpacePowerUserGroupRow,
        mySpaceAdminAssignmentRow,
        myProjectAdminAssignmentRow,
        testProjectAdminGroupRow
      ]
    }
  })

  form.getRolesGrid().getColumns()[4].getLabel().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      columns: [
        {
          name: 'inheritedFrom',
          sort: null
        },
        {
          name: 'level',
          sort: null
        },
        {
          name: 'space',
          sort: null
        },
        {
          name: 'project',
          sort: null
        },
        {
          name: 'role',
          sort: 'asc'
        },
        {
          name: 'registrator',
          sort: null
        },
        {
          name: 'registrationDate',
          sort: null
        }
      ],
      rows: [
        instanceAdminAssignmentRow,
        mySpaceAdminAssignmentRow,
        testProjectAdminGroupRow,
        myProjectAdminAssignmentRow,
        testSpacePowerUserGroupRow,
        instanceObserverGroupRow
      ]
    }
  })
}
