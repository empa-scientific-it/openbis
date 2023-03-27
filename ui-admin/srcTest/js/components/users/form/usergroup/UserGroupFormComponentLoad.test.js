import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('load new', testLoadNew)
  test('load existing', testLoadExisting)
})

async function testLoadNew() {
  const form = await common.mountNew()

  form.expectJSON({
    usersGrid: {
      rows: []
    },
    rolesGrid: {
      rows: []
    },
    parameters: {
      group: {
        title: 'New Group',
        code: {
          label: 'Code',
          value: null,
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
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
    mySpaceUser,
    testSpaceUser,
    inactiveUser,
    instanceObserverAssignment,
    testSpacePowerUserAssignment,
    testProjectAdminAssignment
  } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')
  group.setDescription('Test Description')
  group.setUsers([mySpaceUser, testSpaceUser, inactiveUser])
  group.setRoleAssignments([
    instanceObserverAssignment,
    testSpacePowerUserAssignment,
    testProjectAdminAssignment
  ])

  const form = await common.mountExisting(group)

  const usersGridJSON = {
    columns: [
      {
        name: 'userId',
        label: 'User Id',
        filter: null,
        sort: 'asc'
      },
      {
        name: 'firstName',
        label: 'First Name',
        filter: null,
        sort: null
      },
      {
        name: 'lastName',
        label: 'Last Name',
        filter: null,
        sort: null
      },
      {
        name: 'email',
        label: 'Email',
        filter: null,
        sort: null
      },
      {
        name: 'space',
        label: 'Home Space',
        filter: null,
        sort: null
      },
      {
        name: 'active',
        label: 'Active',
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
    rows: [inactiveUser, mySpaceUser, testSpaceUser].map(user => ({
      values: {
        userId: user.getUserId(),
        firstName: user.getFirstName(),
        lastName: user.getLastName(),
        email: user.getEmail(),
        space: user.space ? user.space.code : null,
        active: String(user.isActive())
      },
      selected: false
    }))
  }

  const rolesGridJSON = {
    columns: [
      {
        name: 'level',
        label: 'Level',
        filter: null,
        sort: 'asc'
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

  form.expectJSON({
    usersGrid: usersGridJSON,
    rolesGrid: rolesGridJSON,
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: group.getCode(),
          mode: 'view'
        },
        description: {
          label: 'Description',
          value: group.getDescription(),
          mode: 'view'
        }
      }
    },
    buttons: {
      edit: {
        enabled: true
      },
      addUser: null,
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
    usersGrid: usersGridJSON,
    rolesGrid: rolesGridJSON,
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: group.getCode(),
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: group.getDescription(),
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
