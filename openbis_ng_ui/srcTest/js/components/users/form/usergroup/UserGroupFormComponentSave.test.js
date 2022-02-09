import UserGroupFormComponentTest from '@srcTest/js/components/users/form/usergroup/UserGroupFormComponentTest.js'
import UserGroupFormTestData from '@srcTest/js/components/users/form/usergroup/UserGroupFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserGroupFormComponentTest()
  common.beforeEach()
})

describe(UserGroupFormComponentTest.SUITE, () => {
  test('save create group', testSaveCreateGroup)
  test('save update group', testSaveUpdateGroup)
  test('save add user', testSaveAddUser)
  test('save add role', testSaveAddRole)
  test('save remove user', testSaveRemoveUser)
  test('save remove role', testSaveRemoveRole)
})

async function testSaveCreateGroup() {
  const { mySpace, mySpaceUser } = UserGroupFormTestData

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([mySpace]))
  common.facade.loadUsers.mockReturnValue(Promise.resolve([mySpaceUser]))

  const form = await common.mountNew()

  // group
  form.getParameters().getGroup().getCode().change('test-group')
  await form.update()

  form.getParameters().getGroup().getDescription().change('Test Description')
  await form.update()

  // user
  form.getButtons().getAddUser().click()
  await form.update()

  form.getParameters().getUser().getUserId().change(mySpaceUser.getUserId())
  await form.update()

  // role
  form.getButtons().getAddRole().click()
  await form.update()

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.SPACE)
  await form.update()

  form.getParameters().getRole().getSpace().change(mySpace.getCode())
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  // save
  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createGroupOperation({
      code: 'TEST-GROUP',
      description: 'Test Description'
    }),
    createUserAssignmentOperation({
      groupCode: 'TEST-GROUP',
      userId: mySpaceUser.getUserId()
    }),
    createRoleAssignmentOperation({
      groupCode: 'TEST-GROUP',
      spaceCode: mySpace.getCode(),
      role: openbis.Role.ADMIN
    })
  ])
}

async function testSaveUpdateGroup() {
  const { testSpaceUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('TEST-GROUP')
  group.setDescription('Test Description')

  common.facade.loadUsers.mockReturnValue(Promise.resolve([testSpaceUser]))

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  // group
  form.getParameters().getGroup().getDescription().change('Updated Description')
  await form.update()

  // user
  form.getButtons().getAddUser().click()
  await form.update()

  form.getParameters().getUser().getUserId().change(testSpaceUser.getUserId())
  await form.update()

  // role
  form.getButtons().getAddRole().click()
  await form.update()

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.INSTANCE)
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.OBSERVER)
  await form.update()

  // save
  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    updateGroupOperation({
      code: group.getCode(),
      description: 'Updated Description'
    }),
    createUserAssignmentOperation({
      groupCode: group.getCode(),
      userId: testSpaceUser.getUserId()
    }),
    createRoleAssignmentOperation({
      groupCode: group.getCode(),
      role: openbis.Role.OBSERVER
    })
  ])
}

async function testSaveAddUser() {
  const { testSpaceUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('TEST-GROUP')

  common.facade.loadUsers.mockReturnValue(Promise.resolve([testSpaceUser]))

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddUser().click()
  await form.update()

  form.getParameters().getUser().getUserId().change(testSpaceUser.getUserId())
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createUserAssignmentOperation({
      groupCode: group.getCode(),
      userId: testSpaceUser.getUserId()
    })
  ])
}

async function testSaveAddRole() {
  const group = new openbis.AuthorizationGroup()
  group.setCode('TEST-GROUP')

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddRole().click()
  await form.update()

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.INSTANCE)
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createRoleAssignmentOperation({
      groupCode: group.getCode(),
      role: openbis.Role.ADMIN
    })
  ])
}

async function testSaveRemoveUser() {
  const { mySpaceUser, testSpaceUser } = UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('TEST-GROUP')
  group.setUsers([mySpaceUser, testSpaceUser])

  common.facade.loadUsers.mockReturnValue(
    Promise.resolve([mySpaceUser, testSpaceUser])
  )

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getUsersGrid().getRows()[0].click()
  await form.update()

  form.getButtons().getRemove().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    deleteUserAssignmentOperation({
      groupCode: group.getCode(),
      userId: mySpaceUser.getUserId()
    })
  ])
}

async function testSaveRemoveRole() {
  const { instanceObserverAssignment, testSpacePowerUserAssignment } =
    UserGroupFormTestData

  const group = new openbis.AuthorizationGroup()
  group.setCode('TEST-GROUP')
  group.setRoleAssignments([
    instanceObserverAssignment,
    testSpacePowerUserAssignment
  ])

  const form = await common.mountExisting(group)

  form.getButtons().getEdit().click()
  await form.update()

  form.getRolesGrid().getRows()[0].click()
  await form.update()

  form.getButtons().getRemove().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    deleteRoleAssignmentOperation({
      roleAssignmentTechId: instanceObserverAssignment.id.techId
    })
  ])
}

function createGroupOperation({ code, description }) {
  const creation = new openbis.AuthorizationGroupCreation()
  creation.setCode(code)
  creation.setDescription(description)
  return new openbis.CreateAuthorizationGroupsOperation([creation])
}

function updateGroupOperation({ code, description }) {
  const update = new openbis.AuthorizationGroupUpdate()
  update.setAuthorizationGroupId(new openbis.AuthorizationGroupPermId(code))
  update.setDescription(description)
  return new openbis.UpdateAuthorizationGroupsOperation([update])
}

function createUserAssignmentOperation({ groupCode, userId }) {
  const update = new openbis.AuthorizationGroupUpdate()
  update.setAuthorizationGroupId(
    new openbis.AuthorizationGroupPermId(groupCode)
  )
  update.getUserIds().add(new openbis.PersonPermId(userId))
  return new openbis.UpdateAuthorizationGroupsOperation([update])
}

function deleteUserAssignmentOperation({ groupCode, userId }) {
  const update = new openbis.AuthorizationGroupUpdate()
  update.setAuthorizationGroupId(
    new openbis.AuthorizationGroupPermId(groupCode)
  )
  update.getUserIds().remove(new openbis.PersonPermId(userId))
  return new openbis.UpdateAuthorizationGroupsOperation([update])
}

function createRoleAssignmentOperation({
  groupCode,
  role,
  spaceCode,
  projectCode
}) {
  const creation = new openbis.RoleAssignmentCreation()
  creation.setAuthorizationGroupId(
    new openbis.AuthorizationGroupPermId(groupCode)
  )
  creation.setRole(role)

  if (spaceCode && projectCode) {
    creation.setProjectId(
      new openbis.ProjectIdentifier('/' + spaceCode + '/' + projectCode)
    )
  } else if (spaceCode) {
    creation.setSpaceId(new openbis.SpacePermId(spaceCode))
  }

  return new openbis.CreateRoleAssignmentsOperation([creation])
}

function deleteRoleAssignmentOperation({ roleAssignmentTechId }) {
  const id = new openbis.RoleAssignmentTechId(roleAssignmentTechId)
  const options = new openbis.RoleAssignmentDeletionOptions()
  options.setReason('deleted via ng_ui')
  return new openbis.DeleteRoleAssignmentsOperation([id], options)
}

function expectExecuteOperations(expectedOperations) {
  expect(common.facade.executeOperations).toHaveBeenCalledTimes(1)
  const actualOperations = common.facade.executeOperations.mock.calls[0][0]
  expect(actualOperations.length).toEqual(expectedOperations.length)
  actualOperations.forEach((actualOperation, index) => {
    expect(actualOperation).toMatchObject(expectedOperations[index])
  })
}
