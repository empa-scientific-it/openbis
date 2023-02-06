import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('validate user', testValidateUser)
  test('validate group', testValidateGroup)
  test('validate role', testValidateRole)
})

async function testValidateUser() {
  const form = await common.mountNew()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      user: {
        title: 'New User',
        userId: {
          label: 'User Id',
          value: null,
          error: 'User Id cannot be empty',
          enabled: true,
          mode: 'edit'
        },
        homeSpace: {
          label: 'Home Space',
          value: null,
          error: null,
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })
}

async function testValidateGroup() {
  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadGroups.mockReturnValue([])
  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddGroup().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    groupsGrid: {
      rows: [
        {
          values: {
            code: null,
            description: null
          },
          selected: true
        }
      ]
    },
    parameters: {
      group: {
        title: 'Group',
        code: {
          label: 'Code',
          value: null,
          error: 'Code cannot be empty',
          mode: 'edit'
        }
      }
    }
  })
}

async function testValidateRole() {
  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddRole().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: null,
            space: null,
            project: null,
            role: null
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
          value: null,
          error: 'Level cannot be empty',
          mode: 'edit'
        },
        space: null,
        project: null,
        role: null
      }
    }
  })

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.PROJECT)
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            inheritedFrom: null,
            level: openbis.RoleLevel.PROJECT,
            space: null,
            project: null,
            role: null
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
          error: null,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: null,
          error: 'Space cannot be empty',
          mode: 'edit'
        },
        project: {
          label: 'Project',
          value: null,
          error: 'Project cannot be empty',
          mode: 'edit'
        },
        role: {
          label: 'Role',
          value: null,
          error: 'Role cannot be empty',
          mode: 'edit'
        }
      }
    }
  })
}
