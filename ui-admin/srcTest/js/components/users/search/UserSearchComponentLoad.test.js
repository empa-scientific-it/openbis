import UserSearchComponent from '@srcTest/js/components/users/search/UserSearchComponent.js'
import UserSearchTestData from '@srcTest/js/components/users/search/UserSearchTestData.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserSearchComponent()
  common.beforeEach()
})

describe(UserSearchComponent.SUITE, () => {
  test('load with searchText (results found)', async () =>
    await testLoadWithSearchText(true))
  test('load with searchText (no results)', async () =>
    await testLoadWithSearchText(false))
  test('load with objectType (results found)', async () =>
    await testLoadWithObjectType(true))
  test('load with objectType (no results)', async () =>
    await testLoadWithObjectType(false))
})

async function testLoadWithSearchText(resultsFound) {
  const {
    testUser,
    testUser2,
    anotherUser,
    testUserGroup,
    anotherUserGroup,
    instanceAdminRoleAssignment,
    instanceObserverRoleAssignment,
    testSpaceAdminRoleAssignment
  } = UserSearchTestData

  openbis.mockSearchPersons(
    resultsFound ? [testUser, testUser2, anotherUser] : []
  )
  openbis.mockSearchGroups(
    resultsFound ? [testUserGroup, anotherUserGroup] : []
  )

  const form = await common.mount({ searchText: 'test' })

  if (resultsFound) {
    form.expectJSON({
      messages: [],
      users: {
        columns: [
          {
            name: 'userId',
            label: 'User Id'
          },
          {
            name: 'firstName',
            label: 'First Name'
          },
          {
            name: 'lastName',
            label: 'Last Name'
          },
          {
            name: 'email',
            label: 'Email'
          },
          {
            name: 'space',
            label: 'Home Space'
          },
          {
            name: 'active',
            label: 'Active'
          }
        ],
        rows: [
          {
            values: {
              userId: testUser.getUserId(),
              firstName: testUser.getFirstName(),
              lastName: testUser.getLastName(),
              email: testUser.getEmail(),
              space: testUser.space.code,
              active: String(testUser.isActive())
            }
          },
          {
            values: {
              userId: testUser2.getUserId(),
              firstName: testUser2.getFirstName(),
              lastName: testUser2.getLastName(),
              email: null,
              space: null,
              active: null
            }
          }
        ]
      },
      usersRoles: {
        columns: [
          {
            label: 'User',
            name: 'user'
          },
          {
            label: 'Inherited From',
            name: 'inheritedFrom'
          },
          {
            label: 'Level',
            name: 'level'
          },
          {
            label: 'Space',
            name: 'space'
          },
          {
            label: 'Project',
            name: 'project'
          },
          {
            label: 'Role',
            name: 'role'
          }
        ],
        rows: [
          {
            values: {
              inheritedFrom: testUserGroup.code,
              level: instanceObserverRoleAssignment.roleLevel,
              project: '(All)',
              role: instanceObserverRoleAssignment.role,
              space: '(All)',
              user: testUser.userId
            }
          },
          {
            values: {
              inheritedFrom: null,
              level: instanceAdminRoleAssignment.roleLevel,
              project: '(All)',
              role: instanceAdminRoleAssignment.role,
              space: '(All)',
              user: testUser.userId
            }
          },
          {
            values: {
              inheritedFrom: testUserGroup.code,
              level: instanceObserverRoleAssignment.roleLevel,
              project: '(All)',
              role: instanceObserverRoleAssignment.role,
              space: '(All)',
              user: testUser2.userId
            }
          },
          {
            values: {
              inheritedFrom: null,
              level: instanceObserverRoleAssignment.roleLevel,
              project: '(All)',
              role: instanceObserverRoleAssignment.role,
              space: '(All)',
              user: testUser2.userId
            }
          },
          {
            values: {
              inheritedFrom: null,
              level: testSpaceAdminRoleAssignment.roleLevel,
              project: '(All)',
              role: testSpaceAdminRoleAssignment.role,
              space: testSpaceAdminRoleAssignment.space.code,
              user: testUser2.userId
            }
          }
        ]
      },
      userGroups: {
        columns: [
          {
            name: 'code',
            label: 'Code'
          },
          {
            name: 'description',
            label: 'Description'
          }
        ],
        rows: [
          {
            values: {
              code: testUserGroup.getCode(),
              description: testUserGroup.getDescription()
            }
          }
        ]
      },
      userGroupsRoles: {
        columns: [
          {
            label: 'Group',
            name: 'group'
          },
          {
            label: 'Level',
            name: 'level'
          },
          {
            label: 'Space',
            name: 'space'
          },
          {
            label: 'Project',
            name: 'project'
          },
          {
            label: 'Role',
            name: 'role'
          }
        ],
        rows: [
          {
            values: {
              group: testUserGroup.code,
              level: instanceObserverRoleAssignment.roleLevel,
              project: '(All)',
              role: instanceObserverRoleAssignment.role,
              space: '(All)'
            }
          }
        ]
      }
    })
  } else {
    form.expectJSON({
      messages: [
        {
          text: 'No results found',
          type: 'info'
        }
      ],
      users: null,
      usersRoles: null,
      userGroups: null,
      userGroupsRoles: null
    })
  }
}

async function testLoadWithObjectType(resultsFound) {
  const {
    testUser,
    testUser2,
    anotherUser,
    testUserGroup,
    anotherUserGroup,
    instanceAdminRoleAssignment,
    instanceObserverRoleAssignment,
    testSpaceAdminRoleAssignment,
    testProjectObserverRoleAssignment
  } = UserSearchTestData

  openbis.mockSearchPersons(
    resultsFound ? [testUser, testUser2, anotherUser] : []
  )
  openbis.mockSearchGroups(
    resultsFound ? [testUserGroup, anotherUserGroup] : []
  )

  const form = await common.mount({
    objectType: objectTypes.USER
  })

  form.expectJSON({
    messages: [],
    users: {
      columns: [
        {
          name: 'userId',
          label: 'User Id'
        },
        {
          name: 'firstName',
          label: 'First Name'
        },
        {
          name: 'lastName',
          label: 'Last Name'
        },
        {
          name: 'email',
          label: 'Email'
        },
        {
          name: 'space',
          label: 'Home Space'
        },
        {
          name: 'active',
          label: 'Active'
        }
      ],
      rows: resultsFound
        ? [
            {
              values: {
                userId: anotherUser.getUserId(),
                firstName: anotherUser.getFirstName(),
                lastName: anotherUser.getLastName(),
                email: null,
                space: null,
                active: null
              }
            },
            {
              values: {
                userId: testUser.getUserId(),
                firstName: testUser.getFirstName(),
                lastName: testUser.getLastName(),
                email: testUser.getEmail(),
                space: testUser.space.code,
                active: String(testUser.isActive())
              }
            },
            {
              values: {
                userId: testUser2.getUserId(),
                firstName: testUser2.getFirstName(),
                lastName: testUser2.getLastName(),
                email: null,
                space: null,
                active: null
              }
            }
          ]
        : []
    },
    usersRoles: {
      columns: [
        {
          label: 'User',
          name: 'user'
        },
        {
          label: 'Inherited From',
          name: 'inheritedFrom'
        },
        {
          label: 'Level',
          name: 'level'
        },
        {
          label: 'Space',
          name: 'space'
        },
        {
          label: 'Project',
          name: 'project'
        },
        {
          label: 'Role',
          name: 'role'
        }
      ],
      rows: resultsFound
        ? [
            {
              values: {
                inheritedFrom: null,
                level: testProjectObserverRoleAssignment.roleLevel,
                project: testProjectObserverRoleAssignment.project.code,
                role: testProjectObserverRoleAssignment.role,
                space: testProjectObserverRoleAssignment.project.space.code,
                user: anotherUser.userId
              }
            },
            {
              values: {
                inheritedFrom: testUserGroup.code,
                level: instanceObserverRoleAssignment.roleLevel,
                project: '(All)',
                role: instanceObserverRoleAssignment.role,
                space: '(All)',
                user: testUser.userId
              }
            },
            {
              values: {
                inheritedFrom: null,
                level: instanceAdminRoleAssignment.roleLevel,
                project: '(All)',
                role: instanceAdminRoleAssignment.role,
                space: '(All)',
                user: testUser.userId
              }
            },
            {
              values: {
                inheritedFrom: testUserGroup.code,
                level: instanceObserverRoleAssignment.roleLevel,
                project: '(All)',
                role: instanceObserverRoleAssignment.role,
                space: '(All)',
                user: testUser2.userId
              }
            },
            {
              values: {
                inheritedFrom: null,
                level: instanceObserverRoleAssignment.roleLevel,
                project: '(All)',
                role: instanceObserverRoleAssignment.role,
                space: '(All)',
                user: testUser2.userId
              }
            },
            {
              values: {
                inheritedFrom: null,
                level: testSpaceAdminRoleAssignment.roleLevel,
                project: '(All)',
                role: testSpaceAdminRoleAssignment.role,
                space: testSpaceAdminRoleAssignment.space.code,
                user: testUser2.userId
              }
            }
          ]
        : []
    },
    userGroups: null,
    userGroupsRoles: null
  })
}
