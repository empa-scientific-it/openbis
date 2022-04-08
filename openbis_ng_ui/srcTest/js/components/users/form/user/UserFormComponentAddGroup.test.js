import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('add group', testAddGroup)
})

async function testAddGroup() {
  const {
    instanceObserverGroup,
    instanceObserverGroupAssignment,
    testSpacePowerUserGroup,
    testSpacePowerUserGroupAssignment,
    testProjectAdminGroup,
    testProjectAdminGroupAssignment
  } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadGroups.mockReturnValue([
    instanceObserverGroup,
    testSpacePowerUserGroup,
    testProjectAdminGroup
  ])

  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup, testSpacePowerUserGroup])
  )

  const form = await common.mountExisting(user)

  form.expectJSON({
    groupsGrid: {
      rows: [instanceObserverGroup, testSpacePowerUserGroup].map(group => ({
        values: {
          code: group.getCode(),
          description: group.getDescription()
        },
        selected: false
      }))
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
            inheritedFrom: testSpacePowerUserGroup.getCode(),
            level: testSpacePowerUserGroupAssignment.getRoleLevel(),
            space: testSpacePowerUserGroupAssignment.space.code,
            project: '(All)',
            role: testSpacePowerUserGroupAssignment.getRole()
          },
          selected: false
        }
      ]
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddGroup().click()
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [
        new openbis.AuthorizationGroup(),
        instanceObserverGroup,
        testSpacePowerUserGroup
      ].map((group, index) => ({
        values: {
          code: group.getCode(),
          description: group.getDescription()
        },
        selected: index === 0
      }))
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
            inheritedFrom: testSpacePowerUserGroup.getCode(),
            level: testSpacePowerUserGroupAssignment.getRoleLevel(),
            space: testSpacePowerUserGroupAssignment.space.code,
            project: '(All)',
            role: testSpacePowerUserGroupAssignment.getRole()
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

  form
    .getParameters()
    .getGroup()
    .getCode()
    .change(testProjectAdminGroup.getCode())
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [
        instanceObserverGroup,
        testProjectAdminGroup,
        testSpacePowerUserGroup
      ].map((group, index) => ({
        values: {
          code: group.getCode(),
          description: group.getDescription()
        },
        selected: index === 1
      }))
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
        }
      ]
    },
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: testProjectAdminGroup.getCode(),
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
