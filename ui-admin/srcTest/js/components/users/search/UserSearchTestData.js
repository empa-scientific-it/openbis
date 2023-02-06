import openbis from '@srcTest/js/services/openbis.js'

const testSpace = new openbis.Space()
testSpace.setCode('TEST_SPACE')

const testProject = new openbis.Project()
testProject.setCode('TEST_PROJECT')
testProject.setSpace(testSpace)

const instanceAdminRoleAssignment = new openbis.RoleAssignment()
instanceAdminRoleAssignment.setRole(openbis.Role.ADMIN)
instanceAdminRoleAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)

const instanceObserverRoleAssignment = new openbis.RoleAssignment()
instanceObserverRoleAssignment.setRole(openbis.Role.OBSERVER)
instanceObserverRoleAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)

const testSpaceAdminRoleAssignment = new openbis.RoleAssignment()
testSpaceAdminRoleAssignment.setRole(openbis.Role.ADMIN)
testSpaceAdminRoleAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
testSpaceAdminRoleAssignment.setSpace(testSpace)

const testProjectObserverRoleAssignment = new openbis.RoleAssignment()
testProjectObserverRoleAssignment.setRole(openbis.Role.OBSERVER)
testProjectObserverRoleAssignment.setRoleLevel(openbis.RoleLevel.PROJECT)
testProjectObserverRoleAssignment.setProject(testProject)

const testUser = new openbis.Person()
testUser.setUserId('test_user')
testUser.setFirstName('Test First Name')
testUser.setLastName('Test Last Name')
testUser.setEmail('test@email.com')
testUser.setSpace(testSpace)
testUser.setActive(true)
testUser.setRoleAssignments([instanceAdminRoleAssignment])

const testUser2 = new openbis.Person()
testUser2.setUserId('test_user_2')
testUser2.setFirstName('Test First Name 2')
testUser2.setLastName('Test Last Name 2')
testUser2.setRoleAssignments([
  instanceObserverRoleAssignment,
  testSpaceAdminRoleAssignment
])

const anotherUser = new openbis.Person()
anotherUser.setUserId('another_user')
anotherUser.setFirstName('Another First Name')
anotherUser.setLastName('Another Last Name')
anotherUser.setRoleAssignments([testProjectObserverRoleAssignment])

const testUserGroup = new openbis.AuthorizationGroup()
testUserGroup.setCode('TEST_USER_GROUP')
testUserGroup.setDescription('Test Description')
testUserGroup.setRoleAssignments([instanceObserverRoleAssignment])
testUserGroup.setUsers([testUser, testUser2])

const anotherUserGroup = new openbis.AuthorizationGroup()
anotherUserGroup.setCode('ANOTHER_USER_GROUP')
anotherUserGroup.setDescription('Another Description')
anotherUserGroup.setUsers([anotherUserGroup])

export default {
  testUser,
  testUser2,
  anotherUser,
  testUserGroup,
  anotherUserGroup,
  instanceAdminRoleAssignment,
  instanceObserverRoleAssignment,
  testSpaceAdminRoleAssignment,
  testProjectObserverRoleAssignment
}
