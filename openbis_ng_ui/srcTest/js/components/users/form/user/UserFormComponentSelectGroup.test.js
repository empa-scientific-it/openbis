import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('select group', testSelectGroup)
})

async function testSelectGroup() {
  const { instanceObserverGroup, testSpacePowerUserGroup } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')

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

  form.getGroupsGrid().getRows()[0].click()
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [instanceObserverGroup, testSpacePowerUserGroup].map(
        (group, index) => ({
          values: {
            code: group.getCode(),
            description: group.getDescription()
          },
          selected: index === 0
        })
      )
    },
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: instanceObserverGroup.getCode(),
          mode: 'view'
        }
      }
    }
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [instanceObserverGroup, testSpacePowerUserGroup].map(
        (group, index) => ({
          values: {
            code: group.getCode(),
            description: group.getDescription()
          },
          selected: index === 0
        })
      )
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
}
