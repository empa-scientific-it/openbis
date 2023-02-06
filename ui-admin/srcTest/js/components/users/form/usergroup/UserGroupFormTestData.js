import openbis from '@srcTest/js/services/openbis.js'

const mySpace = new openbis.Space()
mySpace.setCode('my-space')

const myProject = new openbis.Project()
myProject.setCode('my-project')
myProject.setSpace(mySpace)

const testSpace = new openbis.Space()
testSpace.setCode('test-space')

const testProject = new openbis.Project()
testProject.setCode('test-project')
testProject.setSpace(testSpace)

const mySpaceUser = new openbis.Person()
mySpaceUser.setUserId('my-space-user')
mySpaceUser.setFirstName('My Space First Name')
mySpaceUser.setLastName('My Space Last Name')
mySpaceUser.setEmail('my-space-user@email.com')
mySpaceUser.setSpace(mySpace)
mySpaceUser.setActive(true)

const testSpaceUser = new openbis.Person()
testSpaceUser.setUserId('test-space-user')
testSpaceUser.setFirstName('Test Space First Name')
testSpaceUser.setLastName('Test Space Last Name')
testSpaceUser.setEmail('test-space-user@email.com')
testSpaceUser.setSpace(testSpace)
testSpaceUser.setActive(true)

const inactiveUser = new openbis.Person()
inactiveUser.setUserId('inactive-user')
inactiveUser.setFirstName('Inactive First Name')
inactiveUser.setLastName('Inactive Last Name')
inactiveUser.setEmail('inactive-user@email.com')
inactiveUser.setActive(false)

const instanceObserverAssignment = new openbis.RoleAssignment()
instanceObserverAssignment.setId(new openbis.RoleAssignmentTechId(101))
instanceObserverAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)
instanceObserverAssignment.setRole(openbis.Role.OBSERVER)

const testSpacePowerUserAssignment = new openbis.RoleAssignment()
testSpacePowerUserAssignment.setId(new openbis.RoleAssignmentTechId(102))
testSpacePowerUserAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
testSpacePowerUserAssignment.setRole(openbis.Role.POWER_USER)
testSpacePowerUserAssignment.setSpace(testSpace)

const testProjectAdminAssignment = new openbis.RoleAssignment()
testProjectAdminAssignment.setId(new openbis.RoleAssignmentTechId(103))
testProjectAdminAssignment.setRoleLevel(openbis.RoleLevel.PROJECT)
testProjectAdminAssignment.setRole(openbis.Role.ADMIN)
testProjectAdminAssignment.setProject(testProject)

const mySpaceAdminAssignment = new openbis.RoleAssignment()
mySpaceAdminAssignment.setId(new openbis.RoleAssignmentTechId(104))
mySpaceAdminAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
mySpaceAdminAssignment.setRole(openbis.Role.ADMIN)
mySpaceAdminAssignment.setSpace(mySpace)

export default {
  mySpace,
  myProject,
  testSpace,
  testProject,
  mySpaceUser,
  testSpaceUser,
  inactiveUser,
  instanceObserverAssignment,
  testSpacePowerUserAssignment,
  testProjectAdminAssignment,
  mySpaceAdminAssignment
}
