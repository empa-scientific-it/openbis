''' 
A dropbox that creates a space. If the request comes from a custom import,  the user that 
intiates the request is given space admin privileges on the space and the user's group
is given power user privileges.
''' 

import ch.systemsx.cisd.etlserver.registrator.api.v2
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy import RoleCode

def create_space(tr):
  """Create a space and return it"""
  incoming = tr.getIncoming().getName()
  space_name = incoming

  userId = tr.getUserId()
  space = tr.createNewSpace(space_name, userId)
  space.setDescription("Space from dropbox")
  return space

def assign_roles_to_space(tr, space):
  """Give the users authorization groups power user privileges on the space"""
  service = tr.getAuthorizationService()
  groups = service.listAuthorizationGroupsForUser("agroup_member")
  group_codes = [group.getCode() for group in groups]
  tr.assignRoleToSpace(RoleCode.POWER_USER, space, None, group_codes)

def assign_users_to_groups(tr, space):
  """Give the users authorization groups power user privileges on the space"""
  service = tr.getAuthorizationService()
  userId = tr.getUserId()
  groups = service.listAuthorizationGroupsForUser("agroup_member")
  group_codes = [group.getCode() for group in groups]
  tr.assignRoleToSpace(RoleCode.POWER_USER, space, None, group_codes)

def process(tr):

  tr.setUserId("test_space")
  space = create_space(tr)
  assign_roles_to_space(tr, space)
