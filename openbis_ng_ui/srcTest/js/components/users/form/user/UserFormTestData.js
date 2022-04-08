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

const instanceObserverGroup = new openbis.AuthorizationGroup()
instanceObserverGroup.setCode('instance-observer-group')

const instanceObserverGroupAssignment = new openbis.RoleAssignment()
instanceObserverGroupAssignment.setId(new openbis.RoleAssignmentTechId(101))
instanceObserverGroupAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)
instanceObserverGroupAssignment.setRole(openbis.Role.OBSERVER)

instanceObserverGroup.setRoleAssignments([instanceObserverGroupAssignment])
instanceObserverGroupAssignment.setAuthorizationGroup(instanceObserverGroup)

const testSpacePowerUserGroup = new openbis.AuthorizationGroup()
testSpacePowerUserGroup.setCode('test-space-power-user-group')

const testSpacePowerUserGroupAssignment = new openbis.RoleAssignment()
testSpacePowerUserGroupAssignment.setId(new openbis.RoleAssignmentTechId(102))
testSpacePowerUserGroupAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
testSpacePowerUserGroupAssignment.setRole(openbis.Role.POWER_USER)
testSpacePowerUserGroupAssignment.setSpace(testSpace)

testSpacePowerUserGroup.setRoleAssignments([testSpacePowerUserGroupAssignment])
testSpacePowerUserGroupAssignment.setAuthorizationGroup(testSpacePowerUserGroup)

const testProjectAdminGroup = new openbis.AuthorizationGroup()
testProjectAdminGroup.setCode('test-project-admin-group')

const testProjectAdminGroupAssignment = new openbis.RoleAssignment()
testProjectAdminGroupAssignment.setId(new openbis.RoleAssignmentTechId(103))
testProjectAdminGroupAssignment.setRoleLevel(openbis.RoleLevel.PROJECT)
testProjectAdminGroupAssignment.setRole(openbis.Role.ADMIN)
testProjectAdminGroupAssignment.setProject(testProject)

testProjectAdminGroup.setRoleAssignments([testProjectAdminGroupAssignment])
testProjectAdminGroupAssignment.setAuthorizationGroup(testProjectAdminGroup)

const instanceAdminAssignment = new openbis.RoleAssignment()
instanceAdminAssignment.setId(new openbis.RoleAssignmentTechId(104))
instanceAdminAssignment.setRoleLevel(openbis.RoleLevel.INSTANCE)
instanceAdminAssignment.setRole(openbis.Role.ADMIN)

const mySpaceAdminAssignment = new openbis.RoleAssignment()
mySpaceAdminAssignment.setId(new openbis.RoleAssignmentTechId(105))
mySpaceAdminAssignment.setRoleLevel(openbis.RoleLevel.SPACE)
mySpaceAdminAssignment.setRole(openbis.Role.ADMIN)
mySpaceAdminAssignment.setSpace(mySpace)

const myProjectAdminAssignment = new openbis.RoleAssignment()
myProjectAdminAssignment.setId(new openbis.RoleAssignmentTechId(106))
myProjectAdminAssignment.setRoleLevel(openbis.RoleLevel.PROJECT)
myProjectAdminAssignment.setRole(openbis.Role.ADMIN)
myProjectAdminAssignment.setProject(myProject)

export default {
  mySpace,
  myProject,
  testSpace,
  testProject,
  instanceObserverGroup,
  instanceObserverGroupAssignment,
  testSpacePowerUserGroup,
  testSpacePowerUserGroupAssignment,
  testProjectAdminGroup,
  testProjectAdminGroupAssignment,
  instanceAdminAssignment,
  mySpaceAdminAssignment,
  myProjectAdminAssignment
}
