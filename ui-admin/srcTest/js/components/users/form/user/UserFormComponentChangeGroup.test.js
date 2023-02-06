import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('change group', testChangeGroup)
})

async function testChangeGroup() {
  const {
    instanceObserverGroup,
    instanceObserverGroupAssignment,
    testSpacePowerUserGroup,
    testSpacePowerUserGroupAssignment
  } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadGroups.mockReturnValue([
    instanceObserverGroup,
    testSpacePowerUserGroup
  ])

  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  form.getGroupsGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [
        {
          values: {
            code: instanceObserverGroup.getCode(),
            description: instanceObserverGroup.getDescription()
          },
          selected: true
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
        }
      ]
    },
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: instanceObserverGroup.getCode(),
          mode: 'edit'
        }
      }
    }
  })

  form
    .getParameters()
    .getGroup()
    .getCode()
    .change(testSpacePowerUserGroup.getCode())
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [
        {
          values: {
            code: testSpacePowerUserGroup.getCode(),
            description: testSpacePowerUserGroup.getDescription()
          },
          selected: true
        }
      ]
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
        }
      ]
    },
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: testSpacePowerUserGroup.getCode(),
          mode: 'edit'
        }
      }
    }
  })
}
