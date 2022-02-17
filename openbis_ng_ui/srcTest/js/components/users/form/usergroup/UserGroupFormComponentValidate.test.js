import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('validate group', testValidateGroup)
  test('validate user', testValidateUser)
  test('validate role', testValidateRole)
})

async function testValidateGroup() {
  const form = await common.mountNew()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    parameters: {
      group: {
        title: 'New Group',
        code: {
          label: 'Code',
          value: null,
          error: 'Code cannot be empty',
          enabled: true,
          mode: 'edit'
        },
        description: {
          label: 'Description',
          value: null,
          error: null,
          enabled: true,
          mode: 'edit'
        }
      }
    }
  })
}

async function testValidateUser() {
  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddUser().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    usersGrid: {
      rows: [
        {
          values: {
            userId: null
          },
          selected: true
        }
      ]
    },
    parameters: {
      user: {
        title: 'User',
        userId: {
          label: 'User Id',
          value: null,
          error: 'User Id cannot be empty',
          mode: 'edit'
        }
      }
    }
  })
}

async function testValidateRole() {
  const group = new openbis.AuthorizationGroup()
  group.setCode('test-group')

  const form = await common.mountExisting(group)

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

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.SPACE)
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  form.expectJSON({
    rolesGrid: {
      rows: [
        {
          values: {
            level: openbis.RoleLevel.SPACE,
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
          value: openbis.RoleLevel.SPACE,
          error: null,
          mode: 'edit'
        },
        space: {
          label: 'Space',
          value: null,
          error: 'Space cannot be empty',
          mode: 'edit'
        },
        project: null,
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
