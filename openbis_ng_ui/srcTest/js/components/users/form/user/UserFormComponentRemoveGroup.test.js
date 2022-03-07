import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('remove group', testRemoveGroup)
})

async function testRemoveGroup() {
  const {
    instanceObserverGroup,
    instanceObserverGroupAssignment,
    testSpacePowerUserGroup,
    testSpacePowerUserGroupAssignment,
    mySpaceAdminAssignment
  } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([mySpaceAdminAssignment])

  common.facade.loadGroups.mockReturnValue([
    instanceObserverGroup,
    testSpacePowerUserGroup
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

  form.getGroupsGrid().getRows()[0].click()
  await form.update()

  form.getButtons().getRemove().click()
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [testSpacePowerUserGroup].map(group => ({
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
      message: {
        text: 'You have unsaved changes',
        type: 'warning'
      }
    }
  })
}
