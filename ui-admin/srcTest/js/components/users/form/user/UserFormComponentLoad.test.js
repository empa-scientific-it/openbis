import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await common.mountNew()

  form.expectJSON({
    groupsGrid: {
      rows: []
    },
    rolesGrid: {
      rows: []
    },
    parameters: {
      user: {
        title: 'New User',
        userId: {
          label: 'User Id',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        homeSpace: {
          label: 'Home Space',
          value: null,
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
        enabled: false
      },
      save: {
        enabled: true
      },
      edit: null,
      cancel: null,
      message: null
    }
  })
}

async function testLoadExisting() {
  const {
    mySpace,
    testSpace,
    instanceObserverGroup,
    instanceObserverGroupAssignment,
    testSpacePowerUserGroup,
    testSpacePowerUserGroupAssignment,
    testProjectAdminGroup,
    testProjectAdminGroupAssignment,
    mySpaceAdminAssignment
  } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setFirstName('test-first-name')
  user.setLastName('test-last-name')
  user.setSpace(mySpace)
  user.setActive(true)
  user.setRoleAssignments([mySpaceAdminAssignment])

  common.facade.loadSpaces.mockReturnValue(
    Promise.resolve([mySpace, testSpace])
  )

  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([
      instanceObserverGroup,
      testSpacePowerUserGroup,
      testProjectAdminGroup
    ])
  )

  const form = await common.mountExisting(user)

  const groupsGridJSON = {
    columns: [
      {
        name: 'code',
        label: 'Code',
        filter: null,
        sort: 'asc'
      },
      {
        name: 'description',
        label: 'Description',
        filter: null,
        sort: null
      },
      {
        name: 'registrator',
        label: 'Registrator',
        filter: null,
        sort: null
      },
      {
        name: 'registrationDate',
        label: 'Registration Date',
        filter: null,
        sort: null
      },
      {
        name: 'modificationDate',
        label: 'Modification Date',
        filter: null,
        sort: null
      }
    ],
    rows: [
      instanceObserverGroup,
      testProjectAdminGroup,
      testSpacePowerUserGroup
    ].map(group => ({
      values: {
        code: group.getCode(),
        description: group.getDescription()
      },
      selected: false
    }))
  }

  const rolesGridJSON = {
    columns: [
      {
        name: 'inheritedFrom',
        label: 'Inherited From',
        filter: null,
        sort: 'asc'
      },
      {
        name: 'level',
        label: 'Level',
        filter: null,
        sort: null
      },
      {
        name: 'space',
        label: 'Space',
        filter: null,
        sort: null
      },
      {
        name: 'project',
        label: 'Project',
        filter: null,
        sort: null
      },
      {
        name: 'role',
        label: 'Role',
        filter: null,
        sort: null
      },
      {
        name: 'registrator',
        label: 'Registrator',
        filter: null,
        sort: null
      },
      {
        name: 'registrationDate',
        label: 'Registration Date',
        filter: null,
        sort: null
      }
    ],
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
          inheritedFrom: testProjectAdminGroup.getCode(),
          level: testProjectAdminGroupAssignment.getRoleLevel(),
          space: testProjectAdminGroupAssignment.project.space.code,
          project: testProjectAdminGroupAssignment.project.code,
          role: testProjectAdminGroupAssignment.getRole()
        },
        selected: false
      },
      {
        values: {
          inheritedFrom: testSpacePowerUserGroup.getCode(),
          level: testSpacePowerUserGroupAssignment.getRoleLevel(),
          space: testSpacePowerUserGroupAssignment.space.code,
          project: '(All)',
          role: testSpacePowerUserGroupAssignment.getRole()
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

  form.expectJSON({
    groupsGrid: groupsGridJSON,
    rolesGrid: rolesGridJSON,
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: user.getUserId(),
          mode: 'view'
        },
        firstName: {
          label: 'First Name',
          value: user.getFirstName(),
          mode: 'view'
        },
        lastName: {
          label: 'Last Name',
          value: user.getLastName(),
          mode: 'view'
        },
        email: {
          label: 'Email',
          value: user.getEmail(),
          mode: 'view'
        },
        homeSpace: {
          label: 'Home Space',
          value: user.space.code,
          mode: 'view'
        },
        active: {
          label: 'Active',
          value: user.isActive(),
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addGroup: null,
      addRole: null,
      remove: null,
      save: null,
      cancel: null,
      message: null
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    groupsGrid: groupsGridJSON,
    rolesGrid: rolesGridJSON,
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: user.getUserId(),
          mode: 'edit'
        },
        firstName: {
          label: 'First Name',
          value: user.getFirstName(),
          mode: 'edit'
        },
        lastName: {
          label: 'Last Name',
          value: user.getLastName(),
          mode: 'edit'
        },
        email: {
          label: 'Email',
          value: user.getEmail(),
          mode: 'edit'
        },
        homeSpace: {
          label: 'Home Space',
          value: user.space.code,
          mode: 'edit'
        },
        active: {
          label: 'Active',
          value: user.isActive(),
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
        enabled: false
      },
      save: {
        enabled: true
      },
      cancel: {
        enabled: true
      },
      edit: null,
      message: null
    }
  })
}
