import UserFormComponentTest from '@srcTest/js/components/users/form/user/UserFormComponentTest.js'
import UserFormTestData from '@srcTest/js/components/users/form/user/UserFormTestData.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new UserFormComponentTest()
  common.beforeEach()
})

describe(UserFormComponentTest.SUITE, () => {
  test('save create user', testSaveCreateUser)
  test('save update user', testSaveUpdateUser)
  test('save add group', testSaveAddGroup)
  test('save add role', testSaveAddRole)
  test('save remove group', testSaveRemoveGroup)
  test('save remove role', testSaveRemoveRole)
})

async function testSaveCreateUser() {
  const { mySpace, myProject, instanceObserverGroup } = UserFormTestData

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([mySpace]))
  common.facade.loadProjects.mockReturnValue(Promise.resolve([myProject]))
  common.facade.loadGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )

  const form = await common.mountNew()

  // user
  form.getParameters().getUser().getUserId().change('test-user')
  await form.update()

  form.getParameters().getUser().getHomeSpace().change(mySpace.getCode())
  await form.update()

  // group
  form.getButtons().getAddGroup().click()
  await form.update()

  form
    .getParameters()
    .getGroup()
    .getCode()
    .change(instanceObserverGroup.getCode())
  await form.update()

  // role
  form.getButtons().getAddRole().click()
  await form.update()

  form.getParameters().getRole().getLevel().change(openbis.RoleLevel.PROJECT)
  await form.update()

  form.getParameters().getRole().getSpace().change(mySpace.getCode())
  await form.update()

  form.getParameters().getRole().getProject().change(myProject.getCode())
  await form.update()

  form.getParameters().getRole().getRole().change(openbis.Role.ADMIN)
  await form.update()

  // save
  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createUserOperation({ userId: 'test-user', spaceCode: mySpace.getCode() }),
    createGroupAssignmentOperation({
      userId: 'test-user',
      groupCode: instanceObserverGroup.getCode()
    }),
    createRoleAssignmentOperation({
      userId: 'test-user',
      role: openbis.Role.ADMIN,
      spaceCode: mySpace.getCode(),
      projectCode: myProject.getCode()
    })
  ])
}

async function testSaveUpdateUser() {
  const { mySpace, instanceObserverGroup } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadSpaces.mockReturnValue(Promise.resolve([mySpace]))
  common.facade.loadGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )
  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  // user
  form.getParameters().getUser().getHomeSpace().change(mySpace.getCode())
  await form.update()

  // group
  form.getButtons().getAddGroup().click()
  await form.update()

  form
    .getParameters()
    .getGroup()
    .getCode()
    .change(instanceObserverGroup.getCode())
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
    updateUserOperation({
      userId: user.getUserId(),
      spaceCode: mySpace.getCode()
    }),
    createGroupAssignmentOperation({
      userId: user.getUserId(),
      groupCode: instanceObserverGroup.getCode()
    }),
    createRoleAssignmentOperation({
      userId: user.getUserId(),
      role: openbis.Role.ADMIN,
      spaceCode: mySpace.getCode()
    })
  ])
}

async function testSaveAddGroup() {
  const { instanceObserverGroup } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )
  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  form.getButtons().getAddGroup().click()
  await form.update()

  form
    .getParameters()
    .getGroup()
    .getCode()
    .change(instanceObserverGroup.getCode())
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    createGroupAssignmentOperation({
      userId: user.getUserId(),
      groupCode: instanceObserverGroup.getCode()
    })
  ])
}

async function testSaveAddRole() {
  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

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
      userId: user.getUserId(),
      role: openbis.Role.ADMIN
    })
  ])
}

async function testSaveRemoveGroup() {
  const { instanceObserverGroup } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')

  common.facade.loadGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )
  common.facade.loadUserGroups.mockReturnValue(
    Promise.resolve([instanceObserverGroup])
  )

  const form = await common.mountExisting(user)

  form.getButtons().getEdit().click()
  await form.update()

  form.getGroupsGrid().getRows()[0].click()
  await form.update()

  form.getButtons().getRemove().click()
  await form.update()

  form.getButtons().getSave().click()
  await form.update()

  expectExecuteOperations([
    deleteGroupAssignmentOperation({
      userId: user.getUserId(),
      groupCode: instanceObserverGroup.getCode()
    })
  ])
}

async function testSaveRemoveRole() {
  const { instanceAdminAssignment } = UserFormTestData

  const user = new openbis.Person()
  user.setUserId('test-user')
  user.setRoleAssignments([instanceAdminAssignment])

  common.facade.loadUserGroups.mockReturnValue(Promise.resolve([]))

  const form = await common.mountExisting(user)

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
      roleAssignmentTechId: instanceAdminAssignment.id.techId
    })
  ])
}

function createUserOperation({ userId, spaceCode }) {
  const creation = new openbis.PersonCreation()
  creation.setUserId(userId)
  if (spaceCode) {
    creation.setSpaceId(new openbis.SpacePermId(spaceCode))
  }
  return new openbis.CreatePersonsOperation([creation])
}

function updateUserOperation({ userId, spaceCode }) {
  const update = new openbis.PersonUpdate()
  update.setUserId(new openbis.PersonPermId(userId))
  if (spaceCode) {
    update.setSpaceId(new openbis.SpacePermId(spaceCode))
  }
  return new openbis.UpdatePersonsOperation([update])
}

function createGroupAssignmentOperation({ userId, groupCode }) {
  const update = new openbis.AuthorizationGroupUpdate()
  update.setAuthorizationGroupId(
    new openbis.AuthorizationGroupPermId(groupCode)
  )
  update.getUserIds().add(new openbis.PersonPermId(userId))
  return new openbis.UpdateAuthorizationGroupsOperation([update])
}

function createRoleAssignmentOperation({
  userId,
  role,
  spaceCode,
  projectCode
}) {
  const creation = new openbis.RoleAssignmentCreation()
  creation.setUserId(new openbis.PersonPermId(userId))
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

function deleteGroupAssignmentOperation({ userId, groupCode }) {
  const update = new openbis.AuthorizationGroupUpdate()
  update.setAuthorizationGroupId(
    new openbis.AuthorizationGroupPermId(groupCode)
  )
  update.getUserIds().remove(new openbis.PersonPermId(userId))
  return new openbis.UpdateAuthorizationGroupsOperation([update])
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
